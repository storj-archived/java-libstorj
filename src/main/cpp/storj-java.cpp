/***************************************************************************
 * Copyright (C) 2017-2018 Kaloyan Raev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ***************************************************************************/
#include <jni.h>
#include <string>
#include <cerrno>
#include <storj.h>
#include <nettle/version.h>

#ifdef _WIN32
#include <direct.h>
#endif

#define HANDLE_ERROR();         if (req->error_code) {                                              \
                                    error_callback(env,                                             \
                                                   callbackObject,                                  \
                                                   10000 + req->error_code,                         \
                                                   curl_easy_strerror((CURLcode) req->error_code)); \
                                } else {                                                            \
                                    struct json_object *error;                                      \
                                    json_object_object_get_ex(req->response, "error", &error);      \
                                    error_callback(env,                                             \
                                                   callbackObject,                                  \
                                                   req->status_code,                                \
                                                   json_object_get_string(error));                  \
                                }

#define HANDLE_ERROR_ARG();     if (req->error_code) {                                              \
                                    error_callback(env,                                             \
                                                   callbackObject,                                  \
                                                   arg,                                             \
                                                   10000 + req->error_code,                         \
                                                   curl_easy_strerror((CURLcode) req->error_code)); \
                                } else {                                                            \
                                    struct json_object *error;                                      \
                                    json_object_object_get_ex(req->response, "error", &error);      \
                                    error_callback(env,                                             \
                                                   callbackObject,                                  \
                                                   arg,                                             \
                                                   req->status_code,                                \
                                                   json_object_get_string(error));                  \
                                }

JavaVM* jvm;

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    jvm = vm;

    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    // increase the number of threads in libuv from the default 4 to 64,
    // so the event loop is more responsive while transferring large files
#ifdef _WIN32
    if (!getenv("UV_THREADPOOL_SIZE")) {
        _putenv_s("UV_THREADPOOL_SIZE", "64");
    }
#else
    setenv("UV_THREADPOOL_SIZE", "64", 0);
#endif

    return JNI_VERSION_1_6;
}

int getJNIEnv(JNIEnv **env)
{
    int status = jvm->GetEnv(reinterpret_cast<void**>(env), JNI_VERSION_1_6);
    if (status == JNI_EDETACHED) {
#ifdef __ANDROID_NDK__
        if (jvm->AttachCurrentThread(env, NULL) != 0) {
#else
        if (jvm->AttachCurrentThread(reinterpret_cast<void**>(env), NULL) != 0) {
#endif
            printf("Failed to attach");
        }
        status = 0;
    } else if (status == JNI_ERR) {
        printf("GetEnv: general error");
    } else if (status == JNI_EVERSION) {
        printf("GetEnv: version not supported");
    }
    return status;
}

typedef struct {
    jobject callbackObject;
    char *bucket_id;
    char *file_id;
    char *path;
} download_handle_t;

typedef struct {
    jobject callbackObject;
    char *bucket_id;
    char *file_name;
    char *path;
} upload_handle_t;

static void error_callback(JNIEnv *env, jobject callbackObject, int code, const char *message)
{
    jclass callbackClass = env->GetObjectClass(callbackObject);
    jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                "onError",
                                                "(ILjava/lang/String;)V");
    env->CallVoidMethod(callbackObject,
                        callbackMethod,
                        code,
                        env->NewStringUTF(message));
}

static void error_callback(JNIEnv *env, jobject callbackObject, jstring arg, int code, const char *message)
{
    jclass callbackClass = env->GetObjectClass(callbackObject);
    jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                "onError",
                                                "(Ljava/lang/String;ILjava/lang/String;)V");
    env->CallVoidMethod(callbackObject,
                        callbackMethod,
                        arg,
                        code,
                        env->NewStringUTF(message));
}

static void error_callback_download(JNIEnv *env, download_handle_t *h, int code, const char *message)
{
    jstring fileId = env->NewStringUTF(h->file_id);

    error_callback(env, h->callbackObject, fileId, code, message);

    env->DeleteGlobalRef(h->callbackObject);
    free(h->bucket_id);
    free(h->file_id);
    free(h->path);
    delete h;
}

static void error_callback_upload(JNIEnv *env, upload_handle_t *h, int code, const char *message)
{
    jstring localPath = env->NewStringUTF(h->path);

    error_callback(env, h->callbackObject, localPath, code, message);

    env->DeleteGlobalRef(h->callbackObject);
    free(h->bucket_id);
    free(h->file_name);
    free(h->path);
    delete h;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_storj_libstorj_Storj__1initEnv(
        JNIEnv *env,
        jobject /* instance */,
        jstring proto,
        jstring host,
        jint port,
        jstring user,
        jstring pass,
        jstring mnemonic,
        jstring userAgent,
        jstring proxyUrl,
        jstring caInfoPath)
{
    storj_http_options_t http_options = {
            .user_agent = (userAgent == NULL) ? NULL : env->GetStringUTFChars(userAgent, NULL),
            .proxy_url = (proxyUrl == NULL) ? NULL : env->GetStringUTFChars(proxyUrl, NULL),
            .cainfo_path = (caInfoPath == NULL) ? NULL : env->GetStringUTFChars(caInfoPath, NULL),
            .low_speed_limit = STORJ_LOW_SPEED_LIMIT,
            .low_speed_time = STORJ_LOW_SPEED_TIME,
            .timeout = STORJ_HTTP_TIMEOUT
    };

    storj_bridge_options_t bridge_options = {
            .proto = (proto == NULL) ? "https" : env->GetStringUTFChars(proto, NULL),
            .host  = (host == NULL) ? "api.storj.io" : env->GetStringUTFChars(host, NULL),
            .port  = port,
            .user  = (user == NULL) ? NULL : env->GetStringUTFChars(user, NULL),
            .pass  = (pass == NULL) ? NULL : env->GetStringUTFChars(pass, NULL)
    };

    storj_encrypt_options_t encrypt_options = {
            .mnemonic = (mnemonic == NULL) ? NULL : env->GetStringUTFChars(mnemonic, NULL)
    };

    storj_log_options_t log_options = {
            .logger = NULL,
            .level = 0
    };

    storj_env_t *storj_env = storj_init_env(&bridge_options, &encrypt_options, &http_options, &log_options);

    if (http_options.user_agent)
        env->ReleaseStringUTFChars(userAgent, http_options.user_agent);

    if (http_options.proxy_url)
        env->ReleaseStringUTFChars(proxyUrl, http_options.proxy_url);

    if (http_options.cainfo_path)
        env->ReleaseStringUTFChars(caInfoPath, http_options.cainfo_path);

    if (bridge_options.proto)
        env->ReleaseStringUTFChars(proto, bridge_options.proto);

    if (bridge_options.host)
        env->ReleaseStringUTFChars(host, bridge_options.host);

    if (bridge_options.user)
        env->ReleaseStringUTFChars(user, bridge_options.user);

    if (bridge_options.pass)
        env->ReleaseStringUTFChars(pass, bridge_options.pass);

    if (encrypt_options.mnemonic)
        env->ReleaseStringUTFChars(mnemonic, encrypt_options.mnemonic);

    return (jlong) storj_env;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1destroyEnv(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;
    storj_destroy_env(storj_env);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1runEventLoop(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;
    while (uv_run(storj_env->loop, UV_RUN_ONCE));
}

static void get_buckets_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    get_buckets_request_t *req = (get_buckets_request_t *) work_req->data;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        jobject callbackObject = (jobject) req->handle;

        if (req->status_code != 200 && req->status_code != 304) {
            HANDLE_ERROR();
        } else {
            jclass bucketClass = env->FindClass("io/storj/libstorj/Bucket");
            jobjectArray bucketArray = env->NewObjectArray(req->total_buckets, bucketClass, NULL);
            jmethodID bucketInit = env->GetMethodID(bucketClass,
                                                    "<init>",
                                                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V");

            for (uint32_t i = 0; i < req->total_buckets; i++) {
                storj_bucket_meta_t *bucket = &req->buckets[i];

                jstring id = env->NewStringUTF(bucket->id);
                jstring name = env->NewStringUTF(bucket->name);
                jstring created = env->NewStringUTF(bucket->created);

                jobject bucketObject = env->NewObject(bucketClass,
                                                      bucketInit,
                                                      id,
                                                      name,
                                                      created,
                                                      bucket->decrypted);
                env->SetObjectArrayElement(bucketArray, i, bucketObject);

                env->DeleteLocalRef(bucketObject);
                env->DeleteLocalRef(id);
                env->DeleteLocalRef(name);
                env->DeleteLocalRef(created);
            }

            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onBucketsReceived",
                                                        "([Lio/storj/libstorj/Bucket;)V");
            env->CallVoidMethod(callbackObject, callbackMethod, bucketArray);
        }

        env->DeleteGlobalRef(callbackObject);
    }

    storj_free_get_buckets_request(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1getBuckets(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv,
        jobject callbackObject)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;

    storj_bridge_get_buckets(storj_env,
                             env->NewGlobalRef(callbackObject),
                             get_buckets_callback);
}

static void get_bucket_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    get_bucket_request_t *req = (get_bucket_request_t *) work_req->data;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        jobject callbackObject = (jobject) req->handle;

        if (req->status_code != 200) {
            jstring arg = env->NewStringUTF(strrchr(req->path, '/') + 1);
            HANDLE_ERROR_ARG();
        } else {
            jclass bucketClass = env->FindClass("io/storj/libstorj/Bucket");
            jmethodID bucketInit = env->GetMethodID(bucketClass,
                                                    "<init>",
                                                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V");

            jstring id = env->NewStringUTF(req->bucket->id);
            jstring name = env->NewStringUTF(req->bucket->name);
            jstring created = env->NewStringUTF(req->bucket->created);
            jobject bucketObject = env->NewObject(bucketClass,
                                                  bucketInit,
                                                  id,
                                                  name,
                                                  created,
                                                  req->bucket->decrypted);

            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onBucketReceived",
                                                        "(Lio/storj/libstorj/Bucket;)V");
            env->CallVoidMethod(callbackObject, callbackMethod, bucketObject);
        }

        env->DeleteGlobalRef(callbackObject);
    }

    storj_free_get_bucket_request(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1getBucket(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv,
        jstring bucketId,
        jobject callbackObject)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);

    storj_bridge_get_bucket(storj_env,
                            bucket_id,
                            env->NewGlobalRef(callbackObject),
                            get_bucket_callback);

    env->ReleaseStringUTFChars(bucketId, bucket_id);
}

static void get_bucket_id_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    get_bucket_id_request_t *req = (get_bucket_id_request_t *) work_req->data;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        jobject callbackObject = (jobject) req->handle;
        jstring bucketName = env->NewStringUTF(req->bucket_name);

        if (req->status_code != 200) {
            jstring arg = bucketName;
            HANDLE_ERROR_ARG();
        } else if (req->bucket_id == NULL) {
            error_callback(env, callbackObject, bucketName, STORJ_BRIDGE_JSON_ERROR, "No bucket id in the response");
        } else {
            jstring bucketId = env->NewStringUTF(req->bucket_id);

            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onBucketIdReceived",
                                                        "(Ljava/lang/String;Ljava/lang/String;)V");
            env->CallVoidMethod(callbackObject, callbackMethod, bucketName, bucketId);
        }

        env->DeleteGlobalRef(callbackObject);
    }

    json_object_put(req->response);
    free((char *)req->bucket_name);
    free(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1getBucketId(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv,
        jstring bucketName,
        jobject callbackObject)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;
    const char *bucket_name = env->GetStringUTFChars(bucketName, NULL);

    storj_bridge_get_bucket_id(storj_env,
                               strdup(bucket_name),
                               env->NewGlobalRef(callbackObject),
                               get_bucket_id_callback);

    env->ReleaseStringUTFChars(bucketName, bucket_name);
}

static void create_bucket_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    create_bucket_request_t *req = (create_bucket_request_t *) work_req->data;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        jobject callbackObject = (jobject) req->handle;

        if (req->status_code != 201) {
            jstring arg = env->NewStringUTF(req->bucket_name);
            HANDLE_ERROR_ARG();
        } else {
            jclass bucketClass = env->FindClass("io/storj/libstorj/Bucket");
            jmethodID bucketInit = env->GetMethodID(bucketClass,
                                                    "<init>",
                                                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V");

            jstring id = env->NewStringUTF(req->bucket->id);
            jstring name = env->NewStringUTF(req->bucket->name);
            jstring created = env->NewStringUTF(req->bucket->created);
            jobject bucketObject = env->NewObject(bucketClass,
                                                  bucketInit,
                                                  id,
                                                  name,
                                                  created,
                                                  req->bucket->decrypted);

            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onBucketCreated",
                                                        "(Lio/storj/libstorj/Bucket;)V");
            env->CallVoidMethod(callbackObject, callbackMethod, bucketObject);
        }

        env->DeleteGlobalRef(callbackObject);
    }

    json_object_put(req->response);
    free((char *)req->bucket_name);
    free((char *)req->encrypted_bucket_name);
    free(req->bucket);
    free(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1createBucket(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv,
        jstring bucketName,
        jobject callbackObject)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;
    const char *bucket_name = env->GetStringUTFChars(bucketName, NULL);

    storj_bridge_create_bucket(storj_env,
                               strdup(bucket_name),
                               env->NewGlobalRef(callbackObject),
                               create_bucket_callback);

    env->ReleaseStringUTFChars(bucketName, bucket_name);
}

static void list_files_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    list_files_request_t *req = (list_files_request_t *) work_req->data;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        jobject callbackObject = (jobject) req->handle;
        jstring bucketId = env->NewStringUTF(req->bucket_id);

        if (req->status_code != 200) {
            jstring arg = bucketId;
            HANDLE_ERROR_ARG();
        } else {
            jclass fileClass = env->FindClass("io/storj/libstorj/File");
            jobjectArray fileArray = env->NewObjectArray(req->total_files, fileClass, NULL);
            jmethodID fileInit = env->GetMethodID(fileClass,
                                                  "<init>",
                                                  "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZJLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");

            for (uint32_t i = 0; i < req->total_files; i++) {
                storj_file_meta_t *file = &req->files[i];

                jstring id = (file->id) ? env->NewStringUTF(file->id) : NULL;
                jstring bucketId = (file->bucket_id) ? env->NewStringUTF(file->bucket_id) : NULL;
                jstring filename = (file->filename) ? env->NewStringUTF(file->filename) : NULL;
                jstring created = (file->created) ? env->NewStringUTF(file->created) : NULL;
                jstring mimetype = (file->mimetype) ? env->NewStringUTF(file->mimetype) : NULL;
                jstring erasure = (file->erasure) ? env->NewStringUTF(file->erasure) : NULL;
                jstring index = (file->index) ? env->NewStringUTF(file->index) : NULL;
                jstring hmac = (file->hmac) ? env->NewStringUTF(file->hmac) : NULL;

                jobject fileObject = env->NewObject(fileClass,
                                                    fileInit,
                                                    id,
                                                    bucketId,
                                                    filename,
                                                    created,
                                                    file->decrypted,
                                                    file->size,
                                                    mimetype,
                                                    erasure,
                                                    index,
                                                    hmac);
                env->SetObjectArrayElement(fileArray, i, fileObject);

                env->DeleteLocalRef(fileObject);
                if (id) {
                    env->DeleteLocalRef(id);
                }
                if (bucketId) {
                    env->DeleteLocalRef(bucketId);
                }
                if (filename) {
                    env->DeleteLocalRef(filename);
                }
                if (created) {
                    env->DeleteLocalRef(created);
                }
                if (mimetype) {
                    env->DeleteLocalRef(mimetype);
                }
                if (erasure) {
                    env->DeleteLocalRef(erasure);
                }
                if (index) {
                    env->DeleteLocalRef(index);
                }
                if (hmac) {
                    env->DeleteLocalRef(hmac);
                }
            }

            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onFilesReceived",
                                                        "(Ljava/lang/String;[Lio/storj/libstorj/File;)V");
            env->CallVoidMethod(callbackObject, callbackMethod, bucketId, fileArray);
        }

        env->DeleteGlobalRef(callbackObject);
    }

    free((char *) req->bucket_id);
    storj_free_list_files_request(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1listFiles(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv,
        jstring bucketId,
        jobject callbackObject)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);

    storj_bridge_list_files(storj_env,
                            strdup(bucket_id),
                            env->NewGlobalRef(callbackObject),
                            list_files_callback);

    env->ReleaseStringUTFChars(bucketId, bucket_id);
}

static void get_file_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    get_file_info_request_t *req = (get_file_info_request_t *) work_req->data;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        jobject callbackObject = (jobject) req->handle;

        if (req->status_code != 200) {
            // extract file_id from path
            char *p = strrchr(req->path, '/');
            p[0] = '\0';
            jstring arg = env->NewStringUTF(strrchr(req->path, '/') + 1);
            HANDLE_ERROR_ARG();
        } else {
            jclass fileClass = env->FindClass("io/storj/libstorj/File");
            jmethodID fileInit = env->GetMethodID(fileClass,
                                                  "<init>",
                                                  "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZJLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");

            jstring id = (req->file->id) ? env->NewStringUTF(req->file->id) : NULL;
            jstring bucketId = (req->file->bucket_id) ? env->NewStringUTF(req->file->bucket_id)
                                                      : NULL;
            jstring filename = (req->file->filename) ? env->NewStringUTF(req->file->filename)
                                                     : NULL;
            jstring created = (req->file->created) ? env->NewStringUTF(req->file->created) : NULL;
            jstring mimetype = (req->file->mimetype) ? env->NewStringUTF(req->file->mimetype)
                                                     : NULL;
            jstring erasure = (req->file->erasure) ? env->NewStringUTF(req->file->erasure) : NULL;
            jstring index = (req->file->index) ? env->NewStringUTF(req->file->index) : NULL;
            jstring hmac = (req->file->hmac) ? env->NewStringUTF(req->file->hmac) : NULL;

            jobject fileObject = env->NewObject(fileClass,
                                                fileInit,
                                                id,
                                                bucketId,
                                                filename,
                                                created,
                                                req->file->decrypted,
                                                req->file->size,
                                                mimetype,
                                                erasure,
                                                index,
                                                hmac);

            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onFileReceived",
                                                        "(Lio/storj/libstorj/File;)V");
            env->CallVoidMethod(callbackObject, callbackMethod, fileObject);
        }

        env->DeleteGlobalRef(callbackObject);
    }

    free((char *)req->bucket_id);
    storj_free_get_file_info_request(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1getFile(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv,
        jstring bucketId,
        jstring fileId,
        jobject callbackObject)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);
    const char *file_id = env->GetStringUTFChars(fileId, NULL);

    storj_bridge_get_file_info(storj_env,
                               strdup(bucket_id),
                               file_id,
                               env->NewGlobalRef(callbackObject),
                               get_file_callback);

    env->ReleaseStringUTFChars(bucketId, bucket_id);
    env->ReleaseStringUTFChars(fileId, file_id);
}

static void get_file_id_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    get_file_id_request_t *req = (get_file_id_request_t *) work_req->data;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        jobject callbackObject = (jobject) req->handle;
        jstring fileName = env->NewStringUTF(req->file_name);

        if (req->status_code != 200) {
            jstring arg = fileName;
            HANDLE_ERROR_ARG();
        } else if (req->file_id == NULL) {
            error_callback(env, callbackObject, fileName, STORJ_BRIDGE_JSON_ERROR, "No file id in the response");
        } else {
            jstring fileId = env->NewStringUTF(req->file_id);

            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onFileIdReceived",
                                                        "(Ljava/lang/String;Ljava/lang/String;)V");
            env->CallVoidMethod(callbackObject, callbackMethod, fileName, fileId);
        }

        env->DeleteGlobalRef(callbackObject);
    }

    json_object_put(req->response);
    free((char *)req->bucket_id);
    free((char *)req->file_name);
    free(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1getFileId(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv,
        jstring bucketId,
        jstring fileName,
        jobject callbackObject)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);
    const char *file_name = env->GetStringUTFChars(fileName, NULL);

    storj_bridge_get_file_id(storj_env,
                             strdup(bucket_id),
                             strdup(file_name),
                             env->NewGlobalRef(callbackObject),
                             get_file_id_callback);

    env->ReleaseStringUTFChars(bucketId, bucket_id);
    env->ReleaseStringUTFChars(fileName, file_name);
}

static void download_file_progress_callback(double progress, uint64_t bytes, uint64_t total_bytes, void *handle)
{
    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        download_handle_t *h = (download_handle_t *) handle;

        jclass callbackClass = env->GetObjectClass(h->callbackObject);
        jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                    "onProgress",
                                                    "(Ljava/lang/String;DJJ)V");
        jstring fileId = env->NewStringUTF(h->file_id);

        env->CallVoidMethod(h->callbackObject,
                            callbackMethod,
                            fileId,
                            progress,
                            bytes,
                            total_bytes);

        // this function is called multiple times during file download
        // cleanup is necessary to avoid local reference table overflow
        env->DeleteLocalRef(callbackClass);
        env->DeleteLocalRef(fileId);
    }
}

static void download_file_complete_callback(int status, FILE *fd, void *handle)
{
    fclose(fd);

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        download_handle_t *h = (download_handle_t *) handle;

        if (status) {
            error_callback_download(env, h, status, storj_strerror(status));
        } else {
            jclass callbackClass = env->GetObjectClass(h->callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onComplete",
                                                        "(Ljava/lang/String;Ljava/lang/String;)V");
            jstring fileId = env->NewStringUTF(h->file_id);
            jstring localPath = env->NewStringUTF(h->path);

            env->CallVoidMethod(h->callbackObject,
                                callbackMethod,
                                fileId,
                                localPath);

            env->DeleteGlobalRef(h->callbackObject);
            free(h->bucket_id);
            free(h->file_id);
            free(h->path);
            delete h;
        }
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_storj_libstorj_Storj__1downloadFile(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv,
        jstring bucketId,
        jstring fileId,
        jstring localPath,
        jobject callbackObject)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);
    const char *file_id = env->GetStringUTFChars(fileId, NULL);
    const char *path = env->GetStringUTFChars(localPath, NULL);

    download_handle_t *h = new download_handle_t;
    h->callbackObject = env->NewGlobalRef(callbackObject);
    h->bucket_id = strdup(bucket_id);
    h->file_id = strdup(file_id);
    h->path = strdup(path);

    FILE *fd = NULL;

    if (path) {
        fd = fopen(path, "w+");
    }

    storj_download_state_t *state = NULL;

    if (fd == NULL) {
        error_callback_download(env, h, 20000 + errno, strerror(errno));
    } else {
        state = storj_bridge_resolve_file(storj_env,
                                          h->bucket_id,
                                          h->file_id,
                                          fd,
                                          h,
                                          download_file_progress_callback,
                                          download_file_complete_callback);
        if (!state) {
            error_callback_download(env, h, STORJ_MEMORY_ERROR, storj_strerror(STORJ_MEMORY_ERROR));
        } else if (state->error_status) {
            // The error will be reported in the complete callback.
            // Don't call the error callback here to avoid double free of memory.
        }
    }

    env->ReleaseStringUTFChars(bucketId, bucket_id);
    env->ReleaseStringUTFChars(fileId, file_id);
    env->ReleaseStringUTFChars(localPath, path);

    return (jlong) state;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_storj_libstorj_Storj__1cancelDownload(
        JNIEnv *env,
        jobject /* instance */,
        jlong downloadState)
{
    storj_download_state_t *state = (storj_download_state_t *) downloadState;

    int result = storj_bridge_resolve_file_cancel(state);

    return (jboolean) (result == 0);
}

static void upload_file_progress_callback(double progress, uint64_t bytes, uint64_t total_bytes, void *handle)
{

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        upload_handle_t *h = (upload_handle_t *) handle;

        jclass callbackClass = env->GetObjectClass(h->callbackObject);
        jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                    "onProgress",
                                                    "(Ljava/lang/String;DJJ)V");
        jstring localPath = env->NewStringUTF(h->path);

        env->CallVoidMethod(h->callbackObject,
                            callbackMethod,
                            localPath,
                            progress,
                            bytes,
                            total_bytes);

        // this function is called multiple times during file download
        // cleanup is necessary to avoid local reference table overflow
        env->DeleteLocalRef(callbackClass);
        env->DeleteLocalRef(localPath);
    }
}

static void upload_file_complete_callback(int status, storj_file_meta_t *file, void *handle)
{

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        upload_handle_t *h = (upload_handle_t *) handle;

        if (status) {
            error_callback_upload(env, h, status, storj_strerror(status));
        } else {
            jclass fileClass = env->FindClass("io/storj/libstorj/File");
            jmethodID fileInit = env->GetMethodID(fileClass,
                                                  "<init>",
                                                  "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZJLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");

            jstring id = (file->id) ? env->NewStringUTF(file->id) : NULL;
            jstring bucketId = (file->bucket_id) ? env->NewStringUTF(file->bucket_id) : NULL;
            jstring filename = (file->filename) ? env->NewStringUTF(file->filename) : NULL;
            jstring created = (file->created) ? env->NewStringUTF(file->created) : NULL;
            jstring mimetype = (file->mimetype) ? env->NewStringUTF(file->mimetype) : NULL;
            jstring erasure = (file->erasure) ? env->NewStringUTF(file->erasure) : NULL;
            jstring index = (file->index) ? env->NewStringUTF(file->index) : NULL;
            jstring hmac = (file->hmac) ? env->NewStringUTF(file->hmac) : NULL;

            jobject fileObject = env->NewObject(fileClass,
                                                fileInit,
                                                id,
                                                bucketId,
                                                filename,
                                                created,
                                                file->decrypted,
                                                file->size,
                                                mimetype,
                                                erasure,
                                                index,
                                                hmac);

            jclass callbackClass = env->GetObjectClass(h->callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onComplete",
                                                        "(Ljava/lang/String;Lio/storj/libstorj/File;)V");
            jstring localPath = env->NewStringUTF(h->path);

            env->CallVoidMethod(h->callbackObject,
                                callbackMethod,
                                localPath,
                                fileObject);

            env->DeleteGlobalRef(h->callbackObject);
            free(h->bucket_id);
            free(h->file_name);
            free(h->path);
            delete h;
        }
    }

    storj_free_uploaded_file_info(file);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_storj_libstorj_Storj__1uploadFile(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv,
        jstring bucketId,
        jstring fileName,
        jstring localPath,
        jobject callbackObject)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);
    const char *file_name = env->GetStringUTFChars(fileName, NULL);
    const char *local_path = env->GetStringUTFChars(localPath, NULL);

    upload_handle_t *h = new upload_handle_t;
    h->callbackObject = env->NewGlobalRef(callbackObject);
    h->bucket_id = strdup(bucket_id);
    h->file_name = strdup(file_name);
    h->path = strdup(local_path);

    FILE *fd = fopen(local_path, "r");

    storj_upload_state_t *state = NULL;

    if (!fd) {
        error_callback_upload(env, h, 20000 + errno, strerror(errno));
    } else {
        storj_upload_opts_t upload_opts = {
                .prepare_frame_limit = 1,
                .push_frame_limit = 64,
                .push_shard_limit = 64,
                .rs = true,
                .index = NULL,
                .bucket_id = h->bucket_id,
                .file_name = h->file_name,
                .fd = fd
        };
        state = storj_bridge_store_file(storj_env,
                                        &upload_opts,
                                        h,
                                        upload_file_progress_callback,
                                        upload_file_complete_callback);
        if (!state) {
            error_callback_upload(env, h, STORJ_MEMORY_ERROR, storj_strerror(STORJ_MEMORY_ERROR));
        } else if (state->error_status) {
            // The error will be reported in the complete callback.
            // Don't call the error callback here to avoid double free of memory.
        }
    }

    env->ReleaseStringUTFChars(bucketId, bucket_id);
    env->ReleaseStringUTFChars(fileName, file_name);
    env->ReleaseStringUTFChars(localPath, local_path);

    return (jlong) state;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_storj_libstorj_Storj__1cancelUpload(
        JNIEnv *env,
        jobject /* instance */,
        jlong uploadState)
{
    storj_upload_state_t *state = (storj_upload_state_t *) uploadState;

    int result = storj_bridge_store_file_cancel(state);

    return (jboolean) (result == 0);
}

static void delete_bucket_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    json_request_t *req = (json_request_t *) work_req->data;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        jobject callbackObject = (jobject) req->handle;
        jstring bucketId = env->NewStringUTF(strrchr(req->path, '/') + 1);

        if (req->status_code != 200 && req->status_code != 204) {
            jstring arg = bucketId;
            HANDLE_ERROR_ARG();
        } else {
            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass, "onBucketDeleted", "(Ljava/lang/String;)V");

            env->CallVoidMethod(callbackObject, callbackMethod, bucketId);
        }

        env->DeleteGlobalRef(callbackObject);
    }

    json_object_put(req->response);
    free(req->path);
    free(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1deleteBucket(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv,
        jstring bucketId,
        jobject callbackObject)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);

    storj_bridge_delete_bucket(storj_env,
                               bucket_id,
                               env->NewGlobalRef(callbackObject),
                               delete_bucket_callback);

    env->ReleaseStringUTFChars(bucketId, bucket_id);
}

static void delete_file_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    json_request_t *req = (json_request_t *) work_req->data;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        jobject callbackObject = (jobject) req->handle;
        jstring fileId = env->NewStringUTF(strrchr(req->path, '/') + 1);

        if (req->status_code != 200 && req->status_code != 204) {
            jstring arg = fileId;
            HANDLE_ERROR_ARG();
        } else {
            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass, "onFileDeleted", "(Ljava/lang/String;)V");

            env->CallVoidMethod(callbackObject, callbackMethod, fileId);
        }

        env->DeleteGlobalRef(callbackObject);
    }

    json_object_put(req->response);
    free(req->path);
    free(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1deleteFile(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv,
        jstring bucketId,
        jstring fileId,
        jobject callbackObject)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);
    const char *file_id = env->GetStringUTFChars(fileId, NULL);

    storj_bridge_delete_file(storj_env,
                             bucket_id,
                             file_id,
                             env->NewGlobalRef(callbackObject),
                             delete_file_callback);

    env->ReleaseStringUTFChars(bucketId, bucket_id);
    env->ReleaseStringUTFChars(fileId, file_id);
}

static void register_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    json_request_t *req = (json_request_t *) work_req->data;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        jobject callbackObject = (jobject) req->handle;

        if (req->status_code != 201) {
            HANDLE_ERROR();
        } else {
            struct json_object *email;
            json_object_object_get_ex(req->response, "email", &email);

            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onConfirmationPending",
                                                        "(Ljava/lang/String;)V");
            env->CallVoidMethod(callbackObject,
                                callbackMethod,
                                env->NewStringUTF(json_object_get_string(email)));
        }

        env->DeleteGlobalRef(callbackObject);
    }

    json_object_put(req->response);
    json_object_put(req->body);
    free(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1register(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv,
        jobject callbackObject)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;

    storj_bridge_register(storj_env,
                          storj_env->bridge_options->user,
                          storj_env->bridge_options->pass,
                          env->NewGlobalRef(callbackObject),
                          register_callback);
}

static void get_info_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    json_request_t *req = (json_request_t *) work_req->data;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        jobject callbackObject = (jobject) req->handle;

        if (req->status_code != 200) {
            HANDLE_ERROR();
        } else {
            struct json_object *info;
            json_object_object_get_ex(req->response, "info", &info);
            struct json_object *title;
            json_object_object_get_ex(info, "title", &title);
            struct json_object *description;
            json_object_object_get_ex(info, "description", &description);
            struct json_object *version;
            json_object_object_get_ex(info, "version", &version);
            struct json_object *host;
            json_object_object_get_ex(req->response, "host", &host);

            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onInfoReceived",
                                                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
            env->CallVoidMethod(callbackObject,
                                callbackMethod,
                                env->NewStringUTF(json_object_get_string(title)),
                                env->NewStringUTF(json_object_get_string(description)),
                                env->NewStringUTF(json_object_get_string(version)),
                                env->NewStringUTF(json_object_get_string(host)));
        }

        env->DeleteGlobalRef(callbackObject);
    }

    json_object_put(req->response);
    free(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1getInfo(
        JNIEnv *env,
        jobject /* instance */,
        jlong storjEnv,
        jobject callbackObject)
{
    storj_env_t *storj_env = (storj_env_t *) storjEnv;

    storj_bridge_get_info(storj_env,
                          env->NewGlobalRef(callbackObject),
                          get_info_callback);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_io_storj_libstorj_Storj__1exportKeys(
        JNIEnv *env,
        jobject /* instance */,
        jstring location_,
        jstring passphrase_)
{
    const char *location = env->GetStringUTFChars(location_, NULL);
    const char *passphrase = env->GetStringUTFChars(passphrase_, NULL);
    char *user = NULL;
    char *pass = NULL;
    char *mnemonic = NULL;

    jobject keysObject = NULL;
    if (!storj_decrypt_read_auth(location, passphrase, &user, &pass, &mnemonic)) {
        jclass keysClass = env->FindClass("io/storj/libstorj/Keys");
        jmethodID keysInit = env->GetMethodID(keysClass,
                                              "<init>",
                                              "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
        keysObject = env->NewObject(keysClass,
                                    keysInit,
                                    env->NewStringUTF(user),
                                    env->NewStringUTF(pass),
                                    env->NewStringUTF(mnemonic));
    }

    if (user) {
        free(user);
    }
    if (pass) {
        free(pass);
    }
    if (mnemonic) {
        free(mnemonic);
    }
    env->ReleaseStringUTFChars(location_, location);
    env->ReleaseStringUTFChars(passphrase_, passphrase);

    return keysObject;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_storj_libstorj_Storj__1writeAuthFile(
        JNIEnv *env,
        jobject /* instance */,
        jstring location_,
        jstring user_,
        jstring pass_,
        jstring mnemonic_,
        jstring passphrase_) {
    const char *location = env->GetStringUTFChars(location_, NULL);
    const char *user = env->GetStringUTFChars(user_, NULL);
    const char *pass = env->GetStringUTFChars(pass_, NULL);
    const char *mnemonic = env->GetStringUTFChars(mnemonic_, NULL);
    const char *passphrase = env->GetStringUTFChars(passphrase_, NULL);

    int result = storj_encrypt_write_auth(location, passphrase, user, pass, mnemonic);

    env->ReleaseStringUTFChars(location_, location);
    env->ReleaseStringUTFChars(user_, user);
    env->ReleaseStringUTFChars(pass_, pass);
    env->ReleaseStringUTFChars(mnemonic_, mnemonic);
    env->ReleaseStringUTFChars(passphrase_, passphrase);

    return (jboolean) (result == 0);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_storj_libstorj_Storj__1getErrorMessage(
        JNIEnv *env,
        jclass /* clazz */,
        jint code) {
    const char *msg = NULL;

    if (code < 10000) {
        msg = storj_strerror(code);
    } else if (code >= 10000 && code < 20000) {
        msg = curl_easy_strerror((CURLcode) (code - 10000));
    } else {
        msg = strerror(code - 20000);
    }

    return env->NewStringUTF(msg);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_storj_libstorj_Storj_checkMnemonic(
        JNIEnv *env,
        jclass /* clazz */,
        jstring mnemonic_) {
    const char *mnemonic = env->GetStringUTFChars(mnemonic_, NULL);
    bool result = storj_mnemonic_check(mnemonic);
    env->ReleaseStringUTFChars(mnemonic_, mnemonic);
    return (jboolean) result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_storj_libstorj_Storj_generateMnemonic(
        JNIEnv *env,
        jclass /* clazz */,
        jint strength) {
    char *mnemonic = NULL;
    storj_mnemonic_generate(strength, &mnemonic);
    return env->NewStringUTF(mnemonic);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_storj_libstorj_Storj_getTimestamp(
        JNIEnv * /* env */,
        jclass /* clazz */) {
    return storj_util_timestamp();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_storj_libstorj_NativeLibraries_getJsonCVersion(
        JNIEnv *env,
        jclass /* clazz */) {
    return env->NewStringUTF(json_c_version());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_storj_libstorj_NativeLibraries_getCurlVersion(
        JNIEnv *env,
        jclass /* clazz */) {
    return env->NewStringUTF(curl_version());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_storj_libstorj_NativeLibraries_getLibuvVersion(
        JNIEnv *env,
        jclass /* clazz */) {
    return env->NewStringUTF(uv_version_string());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_storj_libstorj_NativeLibraries_getNettleVersion(
        JNIEnv *env,
        jclass /* clazz */) {
    char version[5];
    sprintf(version, "%d.%d", nettle_version_major(), nettle_version_minor());
    return env->NewStringUTF(version);
}
