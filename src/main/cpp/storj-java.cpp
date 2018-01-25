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
#include <storj.h>
#include <nettle/version.h>

#ifdef _WIN32
#include <direct.h>
#endif

#define INIT_ENV_ERROR "Failed to initialize Storj environment"

JavaVM* jvm;

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    jvm = vm;

    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    return JNI_VERSION_1_6;
}

int getJNIEnv(JNIEnv **env) {
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
} jcallback_t;

typedef struct {
    jcallback_t base;
    jstring fileId;
    jstring localPath;
} jdownload_callback_t;

typedef struct {
    jcallback_t base;
    jstring filePath;
} jupload_callback_t;

static void error_callback(JNIEnv *env, jobject callbackObject, const char *message) {
    jclass callbackClass = env->GetObjectClass(callbackObject);
    jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                "onError",
                                                "(Ljava/lang/String;)V");
    env->CallVoidMethod(callbackObject,
                        callbackMethod,
                        env->NewStringUTF(message));
}

static void error_callback(JNIEnv *env, jobject callbackObject, jstring arg, const char *message) {
    jclass callbackClass = env->GetObjectClass(callbackObject);
    jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                "onError",
                                                "(Ljava/lang/String;Ljava/lang/String;)V");
    env->CallVoidMethod(callbackObject,
                        callbackMethod,
                        arg,
                        env->NewStringUTF(message));
}

static storj_env_t *init_env(JNIEnv *env, jobject storjEnv)
{
    jclass envClass = env->GetObjectClass(storjEnv);

    jfieldID fidUserAgent = env->GetFieldID(envClass, "userAgent", "Ljava/lang/String;");
    jstring userAgent = (jstring) env->GetObjectField(storjEnv, fidUserAgent);

    jfieldID fidProxyUrl = env->GetFieldID(envClass, "proxyUrl", "Ljava/lang/String;");
    jstring proxyUrl = (jstring) env->GetObjectField(storjEnv, fidProxyUrl);

    jfieldID fidCaInfoPath = env->GetFieldID(envClass, "caInfoPath", "Ljava/lang/String;");
    jstring caInfoPath = (jstring) env->GetObjectField(storjEnv, fidCaInfoPath);

    jfieldID fidProto = env->GetFieldID(envClass, "proto", "Ljava/lang/String;");
    jstring proto = (jstring) env->GetObjectField(storjEnv, fidProto);

    jfieldID fidHost = env->GetFieldID(envClass, "host", "Ljava/lang/String;");
    jstring host = (jstring) env->GetObjectField(storjEnv, fidHost);

    jfieldID fidPort = env->GetFieldID(envClass, "port", "I");
    jint port = env->GetIntField(storjEnv, fidPort);

    jfieldID fidUser = env->GetFieldID(envClass, "user", "Ljava/lang/String;");
    jstring user = (jstring) env->GetObjectField(storjEnv, fidUser);

    jfieldID fidPass = env->GetFieldID(envClass, "pass", "Ljava/lang/String;");
    jstring pass = (jstring) env->GetObjectField(storjEnv, fidPass);

    jfieldID fidMnemonic = env->GetFieldID(envClass, "mnemonic", "Ljava/lang/String;");
    jstring mnemonic = (jstring) env->GetObjectField(storjEnv, fidMnemonic);

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

    return storj_env;
}

static void get_buckets_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    get_buckets_request_t *req = (get_buckets_request_t *) work_req->data;
    jcallback_t *jcallback = (jcallback_t *) req->handle;
    jobject callbackObject = jcallback->callbackObject;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        if (req->status_code != 200 && req->status_code != 304) {
            char error_message[256];
            if (req->status_code == 401) {
                strcpy(error_message, "Invalid user credentials");
            } else {
                sprintf(error_message, "Request failed with status code: %i", req->status_code);
            }
            error_callback(env, callbackObject, error_message);
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

            env->DeleteGlobalRef(callbackObject);
        }
    }

    storj_free_get_buckets_request(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1getBuckets(
        JNIEnv *env,
        jobject /* instance */,
        jobject storjEnv,
        jobject callbackObject) {
    storj_env_t *storj_env = init_env(env, storjEnv);

    if (!storj_env) {
        error_callback(env, callbackObject, INIT_ENV_ERROR);
    } else {
        jcallback_t jcallback = {
                .callbackObject = env->NewGlobalRef(callbackObject)
        };
        storj_bridge_get_buckets(storj_env, &jcallback, get_buckets_callback);

        uv_run(storj_env->loop, UV_RUN_DEFAULT);

        storj_destroy_env(storj_env);
    }
}

static void get_bucket_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    get_bucket_request_t *req = (get_bucket_request_t *) work_req->data;
    jcallback_t *jcallback = (jcallback_t *) req->handle;
    jobject callbackObject = jcallback->callbackObject;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        if (req->status_code != 200) {
            char error_message[256];
            char *bucket_id = strrchr(req->path, '/') + 1;
            if (req->status_code == 404) {
                sprintf(error_message, "Bucket id [%s] does not exist", bucket_id);
            } else if (req->status_code == 400) {
                sprintf(error_message, "Bucket id [%s] is invalid", bucket_id);
            } else if (req->status_code == 401) {
                strcpy(error_message, "Invalid user credentials");
            } else {
                sprintf(error_message, "Failed to retrieve bucket. (%i)", req->status_code);
            }
            error_callback(env, callbackObject, error_message);
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

            env->DeleteGlobalRef(callbackObject);
        }
    }

    storj_free_get_bucket_request(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1getBucket(
        JNIEnv *env,
        jobject /* instance */,
        jobject storjEnv,
        jstring bucketId,
        jobject callbackObject) {
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);

    storj_env_t *storj_env = init_env(env, storjEnv);

    if (!storj_env) {
        error_callback(env, callbackObject, INIT_ENV_ERROR);
    } else {
        jcallback_t jcallback = {
                .callbackObject = env->NewGlobalRef(callbackObject)
        };
        storj_bridge_get_bucket(storj_env, bucket_id, &jcallback, get_bucket_callback);

        uv_run(storj_env->loop, UV_RUN_DEFAULT);

        storj_destroy_env(storj_env);
    }

    env->ReleaseStringUTFChars(bucketId, bucket_id);
}

static void get_bucket_id_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    get_bucket_id_request_t *req = (get_bucket_id_request_t *) work_req->data;
    jcallback_t *jcallback = (jcallback_t *) req->handle;
    jobject callbackObject = jcallback->callbackObject;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        if (req->status_code != 200) {
            char error_message[256];
            if (req->status_code == 404) {
                sprintf(error_message, "Cannot get id for bucket with name [%s]. Name does not exist.", req->bucket_name);
            } else if (req->status_code == 401) {
                strcpy(error_message, "Invalid user credentials");
            } else {
                sprintf(error_message, "Request failed with status code: %i", req->status_code);
            }
            error_callback(env, callbackObject, error_message);
        } else if (req->bucket_id == NULL) {
            error_callback(env, callbackObject, "Failed to get bucket id");
        } else {
            jstring bucketId = env->NewStringUTF(req->bucket_id);

            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onBucketIdReceived",
                                                        "(Ljava/lang/String;)V");
            env->CallVoidMethod(callbackObject, callbackMethod, bucketId);

            env->DeleteGlobalRef(callbackObject);
        }
    }

    json_object_put(req->response);
    free(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1getBucketId(
        JNIEnv *env,
        jobject /* instance */,
        jobject storjEnv,
        jstring bucketName,
        jobject callbackObject) {
    const char *bucket_name = env->GetStringUTFChars(bucketName, NULL);

    storj_env_t *storj_env = init_env(env, storjEnv);

    if (!storj_env) {
        error_callback(env, callbackObject, INIT_ENV_ERROR);
    } else {
        jcallback_t jcallback = {
                .callbackObject = env->NewGlobalRef(callbackObject)
        };
        storj_bridge_get_bucket_id(storj_env, bucket_name, &jcallback, get_bucket_id_callback);

        uv_run(storj_env->loop, UV_RUN_DEFAULT);

        storj_destroy_env(storj_env);
    }

    env->ReleaseStringUTFChars(bucketName, bucket_name);
}

static void create_bucket_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    create_bucket_request_t *req = (create_bucket_request_t *) work_req->data;
    jcallback_t *jcallback = (jcallback_t *) req->handle;
    jobject callbackObject = jcallback->callbackObject;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        if (req->status_code != 201) {
            char error_message[256];
            if (req->status_code == 404) {
                sprintf(error_message, "Cannot create bucket [%s]. Name already exists.",
                        req->bucket->name);
            } else if (req->status_code == 401) {
                strcpy(error_message, "Invalid user credentials");
            } else {
                sprintf(error_message, "Request failed with status code: %i", req->status_code);
            }
            error_callback(env, callbackObject, error_message);
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

            env->DeleteGlobalRef(callbackObject);
        }
    }

    json_object_put(req->response);
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
        jobject storjEnv,
        jstring bucketName,
        jobject callbackObject) {
    const char *bucket_name = env->GetStringUTFChars(bucketName, NULL);

    storj_env_t *storj_env = init_env(env, storjEnv);

    if (!storj_env) {
        error_callback(env, callbackObject, INIT_ENV_ERROR);
    } else {
        jcallback_t jcallback = {
                .callbackObject = env->NewGlobalRef(callbackObject)
        };
        storj_bridge_create_bucket(storj_env, bucket_name, &jcallback, create_bucket_callback);

        uv_run(storj_env->loop, UV_RUN_DEFAULT);

        storj_destroy_env(storj_env);
    }

    env->ReleaseStringUTFChars(bucketName, bucket_name);
}

static void list_files_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    list_files_request_t *req = (list_files_request_t *) work_req->data;
    jcallback_t *jcallback = (jcallback_t *) req->handle;
    jobject callbackObject = jcallback->callbackObject;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        if (req->status_code != 200) {
            char error_message[256];
            if (req->status_code == 404) {
                sprintf(error_message, "Bucket id [%s] does not exist", req->bucket_id);
            } else if (req->status_code == 400) {
                sprintf(error_message, "Bucket id [%s] is invalid", req->bucket_id);
            } else if (req->status_code == 401) {
                strcpy(error_message, "Invalid user credentials");
            } else {
                sprintf(error_message, "Request failed with status code: %i", req->status_code);
            }
            error_callback(env, callbackObject, error_message);
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
                                                        "([Lio/storj/libstorj/File;)V");
            env->CallVoidMethod(callbackObject, callbackMethod, fileArray);

            env->DeleteGlobalRef(callbackObject);
        }
    }

    storj_free_list_files_request(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1listFiles(
        JNIEnv *env,
        jobject /* instance */,
        jobject storjEnv,
        jstring bucketId,
        jobject callbackObject) {
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);

    storj_env_t *storj_env = init_env(env, storjEnv);

    if (!storj_env) {
        error_callback(env, callbackObject, INIT_ENV_ERROR);
    } else {
        jcallback_t jcallback = {
                .callbackObject = env->NewGlobalRef(callbackObject)
        };
        storj_bridge_list_files(storj_env, bucket_id, &jcallback, list_files_callback);

        uv_run(storj_env->loop, UV_RUN_DEFAULT);

        storj_destroy_env(storj_env);
    }

    env->ReleaseStringUTFChars(bucketId, bucket_id);
}

static void get_file_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    get_file_info_request_t *req = (get_file_info_request_t *) work_req->data;
    jcallback_t *jcallback = (jcallback_t *) req->handle;
    jobject callbackObject = jcallback->callbackObject;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        if (req->status_code != 200) {
            char error_message[256];
            if (req->status_code == 404) {
                sprintf(error_message, "Bucket id [%s] or file id [%s] does not exist",
                        req->bucket_id, req->file->id);
            } else if (req->status_code == 400) {
                sprintf(error_message, "Bucket id [%s] or file id [%s] is invalid", req->bucket_id,
                        req->file->id);
            } else if (req->status_code == 401) {
                strcpy(error_message, "Invalid user credentials");
            } else {
                sprintf(error_message, "Request failed with status code: %i", req->status_code);
            }
            error_callback(env, callbackObject, error_message);
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

            env->DeleteGlobalRef(callbackObject);
        }
    }

    storj_free_get_file_info_request(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1getFile(
        JNIEnv *env,
        jobject /* instance */,
        jobject storjEnv,
        jstring bucketId,
        jstring fileId,
        jobject callbackObject) {
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);
    const char *file_id = env->GetStringUTFChars(fileId, NULL);

    storj_env_t *storj_env = init_env(env, storjEnv);

    if (!storj_env) {
        error_callback(env, callbackObject, INIT_ENV_ERROR);
    } else {
        jcallback_t jcallback = {
                .callbackObject = env->NewGlobalRef(callbackObject)
        };
        storj_bridge_get_file_info(storj_env, bucket_id, file_id, &jcallback, get_file_callback);

        uv_run(storj_env->loop, UV_RUN_DEFAULT);

        storj_destroy_env(storj_env);
    }

    env->ReleaseStringUTFChars(bucketId, bucket_id);
    env->ReleaseStringUTFChars(fileId, file_id);
}

static void get_file_id_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    get_file_id_request_t *req = (get_file_id_request_t *) work_req->data;
    jcallback_t *jcallback = (jcallback_t *) req->handle;
    jobject callbackObject = jcallback->callbackObject;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        if (req->status_code != 200) {
            char error_message[256];
            if (req->status_code == 404) {
                sprintf(error_message, "Bucket id [%s] or file name [%s] does not exist",
                        req->bucket_id, req->file_name);
            } else if (req->status_code == 401) {
                strcpy(error_message, "Invalid user credentials");
            } else {
                sprintf(error_message, "Request failed with status code: %i", req->status_code);
            }
            error_callback(env, callbackObject, error_message);
        } else if (req->file_id == NULL) {
            error_callback(env, callbackObject, "Failed to get file id");
        } else {
            jstring fileId = env->NewStringUTF(req->file_id);

            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onFileIdReceived",
                                                        "(Ljava/lang/String;)V");
            env->CallVoidMethod(callbackObject, callbackMethod, fileId);

            env->DeleteGlobalRef(callbackObject);
        }
    }

    json_object_put(req->response);
    free(req);
    free(work_req);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1getFileId(
        JNIEnv *env,
        jobject /* instance */,
        jobject storjEnv,
        jstring bucketId,
        jstring fileName,
        jobject callbackObject) {
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);
    const char *file_name = env->GetStringUTFChars(fileName, NULL);

    storj_env_t *storj_env = init_env(env, storjEnv);

    if (!storj_env) {
        error_callback(env, callbackObject, INIT_ENV_ERROR);
    } else {
        jcallback_t jcallback = {
                .callbackObject = env->NewGlobalRef(callbackObject)
        };
        storj_bridge_get_file_id(storj_env, bucket_id, file_name, &jcallback, get_file_id_callback);

        uv_run(storj_env->loop, UV_RUN_DEFAULT);

        storj_destroy_env(storj_env);
    }

    env->ReleaseStringUTFChars(bucketId, bucket_id);
    env->ReleaseStringUTFChars(fileName, file_name);
}

static void download_file_progress_callback(double progress, uint64_t bytes, uint64_t total_bytes, void *handle)
{
    jcallback_t *jcallback = (jcallback_t *) handle;
    jobject callbackObject = jcallback->callbackObject;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        jclass callbackClass = env->GetObjectClass(callbackObject);
        jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                    "onProgress",
                                                    "(Ljava/lang/String;DJJ)V");

        jdownload_callback_t *cb_extension = (jdownload_callback_t *) handle;
        env->CallVoidMethod(callbackObject,
                            callbackMethod,
                            cb_extension->fileId,
                            progress,
                            bytes,
                            total_bytes);

        // this function is called multiple times during file download
        // cleanup is necessary to avoid local reference table overflow
        env->DeleteLocalRef(callbackClass);
    }
}

static void download_file_complete_callback(int status, FILE *fd, void *handle)
{
    fclose(fd);

    jcallback_t *jcallback = (jcallback_t *) handle;
    jobject callbackObject = jcallback->callbackObject;
    jdownload_callback_t *cb_extension = (jdownload_callback_t *) handle;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        if (status) {
            char error_message[256];
            sprintf(error_message, "Download failed. %s (%d)", storj_strerror(status), status);
            error_callback(env, callbackObject, cb_extension->fileId, error_message);
        } else {
            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onComplete",
                                                        "(Ljava/lang/String;Ljava/lang/String;)V");

            env->CallVoidMethod(callbackObject,
                                callbackMethod,
                                cb_extension->fileId,
                                cb_extension->localPath);

            env->DeleteGlobalRef(callbackObject);
            env->DeleteGlobalRef(cb_extension->fileId);
            env->DeleteGlobalRef(cb_extension->localPath);
        }
    }
}

static int download_file(
        FILE *fd,
        const char *bucket_id,
        const char *file_id,
        storj_env_t *storj_env,
        void *handle)
{
    uv_signal_t *sig = (uv_signal_t *) malloc(sizeof(uv_signal_t));
    if (!sig) {
        return 1;
    }

    uv_signal_init(storj_env->loop, sig);
//  uv_signal_start(sig, download_signal_handler, SIGINT);

    storj_download_state_t *state;
    state = storj_bridge_resolve_file(storj_env,
                                      bucket_id,
                                      file_id,
                                      fd,
                                      handle,
                                      download_file_progress_callback,
                                      download_file_complete_callback);

    sig->data = state;

    return state->error_status;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1downloadFile(
        JNIEnv *env,
        jobject /* instance */,
        jobject storjEnv,
        jstring bucketId,
        jstring fileId,
        jstring localPath,
        jobject callbackObject) {
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);
    const char *file_id = env->GetStringUTFChars(fileId, NULL);
    const char *path = env->GetStringUTFChars(localPath, NULL);

    storj_env_t *storj_env = init_env(env, storjEnv);

    if (!storj_env) {
        error_callback(env, callbackObject, fileId, INIT_ENV_ERROR);
    } else {
        jcallback_t jcallback = {
                .callbackObject = env->NewGlobalRef(callbackObject)
        };

        jdownload_callback_t cb_extension = {
                .base = jcallback,
                .fileId = (jstring) env->NewGlobalRef(fileId),
                .localPath = (jstring) env->NewGlobalRef(localPath)
        };

        FILE *fd = NULL;

        if (path) {
            if (access(path, F_OK) != -1) {
                // TODO ask user if file should be overwritten
                unlink(path);
            }

            fd = fopen(path, "w+");
        }

        if (fd == NULL) {
            char error_message[256];
            sprintf(error_message, "Unable to open %s: %s", path, strerror(errno));
            error_callback(env, callbackObject, fileId, error_message);
        } else if (download_file(fd, bucket_id, file_id, storj_env, &cb_extension)) {
            error_callback(env, callbackObject, fileId, "Can't allocate memory");
        } else {
            uv_run(storj_env->loop, UV_RUN_DEFAULT);
        }

        storj_destroy_env(storj_env);
    }

    env->ReleaseStringUTFChars(bucketId, bucket_id);
    env->ReleaseStringUTFChars(fileId, file_id);
    env->ReleaseStringUTFChars(localPath, path);
}

static void upload_file_progress_callback(double progress, uint64_t bytes, uint64_t total_bytes, void *handle)
{
    jcallback_t *jcallback = (jcallback_t *) handle;
    jobject callbackObject = jcallback->callbackObject;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        jclass callbackClass = env->GetObjectClass(callbackObject);
        jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                    "onProgress",
                                                    "(Ljava/lang/String;DJJ)V");

        jupload_callback_t *cb_extension = (jupload_callback_t *) handle;
        env->CallVoidMethod(callbackObject,
                            callbackMethod,
                            cb_extension->filePath,
                            progress,
                            bytes,
                            total_bytes);

        // this function is called multiple times during file download
        // cleanup is necessary to avoid local reference table overflow
        env->DeleteLocalRef(callbackClass);
    }
}

static void upload_file_complete_callback(int status, storj_file_meta_t *file, void *handle)
{
    jcallback_t *jcallback = (jcallback_t *) handle;
    jobject callbackObject = jcallback->callbackObject;
    jupload_callback_t *cb_extension = (jupload_callback_t *) handle;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        if (status) {
            char error_message[256];
            sprintf(error_message, "Upload failed. %s (%d)", storj_strerror(status), status);
            error_callback(env, callbackObject, cb_extension->filePath, error_message);
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

            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass,
                                                        "onComplete",
                                                        "(Ljava/lang/String;Lio/storj/libstorj/File;)V");

            env->CallVoidMethod(callbackObject,
                                callbackMethod,
                                cb_extension->filePath,
                                fileObject);

            env->DeleteGlobalRef(callbackObject);
            env->DeleteGlobalRef(cb_extension->filePath);
        }
    }

    storj_free_uploaded_file_info(file);
}

static int upload_file(
        FILE *fd,
        const char *bucket_id,
        const char *file_name,
        storj_env_t *storj_env,
        void *handle)
{
    storj_upload_opts_t upload_opts = {
            .prepare_frame_limit = 1,
            .push_frame_limit = 64,
            .push_shard_limit = 64,
            .rs = true,
            .index = NULL,
            .bucket_id = bucket_id,
            .file_name = file_name,
            .fd = fd
    };

    uv_signal_t *sig = (uv_signal_t *) malloc(sizeof(uv_signal_t));
    if (!sig) {
        return 1;
    }

    uv_signal_init(storj_env->loop, sig);
//  uv_signal_start(sig, upload_signal_handler, SIGINT);

    storj_upload_state_t *state;
    state = storj_bridge_store_file(storj_env,
                                    &upload_opts,
                                    handle,
                                    upload_file_progress_callback,
                                    upload_file_complete_callback);
    sig->data = state;

    return state->error_status;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_storj_libstorj_Storj__1uploadFile(
        JNIEnv *env,
        jobject /* instance */,
        jobject storjEnv,
        jstring bucketId,
        jstring fileName,
        jstring localPath,
        jobject callbackObject) {
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);
    const char *file_name = env->GetStringUTFChars(fileName, NULL);
    const char *local_path = env->GetStringUTFChars(localPath, NULL);

    storj_env_t *storj_env = init_env(env, storjEnv);

    if (!storj_env) {
        error_callback(env, callbackObject, localPath, INIT_ENV_ERROR);
    } else {
        jcallback_t jcallback = {
                .callbackObject = env->NewGlobalRef(callbackObject)
        };

        jupload_callback_t cb_extension = {
                .base = jcallback,
                .filePath = (jstring) env->NewGlobalRef(localPath)
        };

        FILE *fd = fopen(local_path, "r");

        if (!fd) {
            error_callback(env, callbackObject, localPath, "Can't read file");
        } else if (upload_file(fd, bucket_id, file_name, storj_env, &cb_extension)) {
            error_callback(env, callbackObject, localPath, "Can't allocate memory");
        } else {
            uv_run(storj_env->loop, UV_RUN_DEFAULT);
        }

        storj_destroy_env(storj_env);
    }

    env->ReleaseStringUTFChars(bucketId, bucket_id);
    env->ReleaseStringUTFChars(fileName, file_name);
    env->ReleaseStringUTFChars(localPath, local_path);
}

static void delete_bucket_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    json_request_t *req = (json_request_t *) work_req->data;
    jcallback_t *jcallback = (jcallback_t *) req->handle;
    jobject callbackObject = jcallback->callbackObject;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        if (req->status_code != 200 && req->status_code != 204) {
            char error_message[256];
            if (req->status_code == 401) {
                strcpy(error_message, "Invalid user credentials");
            } else {
                sprintf(error_message, "Failed to destroy bucket. (%i)", req->status_code);
            }
            error_callback(env, callbackObject, error_message);
        } else {
            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass, "onBucketDeleted", "()V");

            env->CallVoidMethod(callbackObject, callbackMethod);

            env->DeleteGlobalRef(callbackObject);
        }
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
        jobject storjEnv,
        jstring bucketId,
        jobject callbackObject) {
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);

    storj_env_t *storj_env = init_env(env, storjEnv);

    if (!storj_env) {
        error_callback(env, callbackObject, INIT_ENV_ERROR);
    } else {
        jcallback_t jcallback = {
                .callbackObject = env->NewGlobalRef(callbackObject)
        };
        storj_bridge_delete_bucket(storj_env, bucket_id, &jcallback, delete_bucket_callback);

        uv_run(storj_env->loop, UV_RUN_DEFAULT);

        storj_destroy_env(storj_env);
    }

    env->ReleaseStringUTFChars(bucketId, bucket_id);
}

static void delete_file_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    json_request_t *req = (json_request_t *) work_req->data;
    jcallback_t *jcallback = (jcallback_t *) req->handle;
    jobject callbackObject = jcallback->callbackObject;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        if (req->status_code != 200 && req->status_code != 204) {
            char error_message[256];
            if (req->status_code == 401) {
                strcpy(error_message, "Invalid user credentials");
            } else {
                sprintf(error_message, "Failed to remove file from bucket. (%i)", req->status_code);
            }
            error_callback(env, callbackObject, error_message);
        } else {
            jclass callbackClass = env->GetObjectClass(callbackObject);
            jmethodID callbackMethod = env->GetMethodID(callbackClass, "onFileDeleted", "()V");

            env->CallVoidMethod(callbackObject, callbackMethod);

            env->DeleteGlobalRef(callbackObject);
        }
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
        jobject storjEnv,
        jstring bucketId,
        jstring fileId,
        jobject callbackObject) {
    const char *bucket_id = env->GetStringUTFChars(bucketId, NULL);
    const char *file_id = env->GetStringUTFChars(fileId, NULL);

    storj_env_t *storj_env = init_env(env, storjEnv);

    if (!storj_env) {
        error_callback(env, callbackObject, INIT_ENV_ERROR);
    } else {
        jcallback_t jcallback = {
                .callbackObject = env->NewGlobalRef(callbackObject)
        };
        storj_bridge_delete_file(storj_env, bucket_id, file_id, &jcallback, delete_file_callback);

        uv_run(storj_env->loop, UV_RUN_DEFAULT);

        storj_destroy_env(storj_env);
    }

    env->ReleaseStringUTFChars(bucketId, bucket_id);
    env->ReleaseStringUTFChars(fileId, file_id);
}

static void register_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    json_request_t *req = (json_request_t *) work_req->data;
    jcallback_t *jcallback = (jcallback_t *) req->handle;
    jobject callbackObject = jcallback->callbackObject;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        if (req->status_code != 201) {
            struct json_object *error;
            json_object_object_get_ex(req->response, "error", &error);
            char error_message[256];
            sprintf(error_message,
                    "Request failed with status code: %i. Error: %s",
                    req->status_code,
                    json_object_get_string(error));
            error_callback(env, callbackObject, error_message);
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

            env->DeleteGlobalRef(callbackObject);
        }
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
        jobject storjEnv,
        jobject callbackObject) {
    storj_env_t *storj_env = init_env(env, storjEnv);

    if (!storj_env) {
        error_callback(env, callbackObject, INIT_ENV_ERROR);
    } else {
        jcallback_t jcallback = {
                .callbackObject = env->NewGlobalRef(callbackObject)
        };
        storj_bridge_register(storj_env,
                              storj_env->bridge_options->user,
                              storj_env->bridge_options->pass,
                              &jcallback,
                              register_callback);

        uv_run(storj_env->loop, UV_RUN_DEFAULT);

        storj_destroy_env(storj_env);
    }
}

static void get_info_callback(uv_work_t *work_req, int status)
{
    assert(status == 0);
    json_request_t *req = (json_request_t *) work_req->data;
    jcallback_t *jcallback = (jcallback_t *) req->handle;
    jobject callbackObject = jcallback->callbackObject;

    JNIEnv *env;
    getJNIEnv(&env);

    if (env != NULL) {
        if (req->error_code || req->response == NULL) {
            char error_message[256];
            if (req->error_code) {
                sprintf(error_message, "Request failed, reason: %s",
                        curl_easy_strerror((CURLcode) req->error_code));
            } else {
                strcpy(error_message, "Failed to get info.");
            }
            error_callback(env, callbackObject, error_message);
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

            env->DeleteGlobalRef(callbackObject);
        }
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
        jobject storjEnv,
        jobject callbackObject) {
    storj_env_t *storj_env = init_env(env, storjEnv);

    if (!storj_env) {
        error_callback(env, callbackObject, INIT_ENV_ERROR);
    } else {
        jcallback_t jcallback = {
                .callbackObject = env->NewGlobalRef(callbackObject)
        };
        storj_bridge_get_info(storj_env, &jcallback, get_info_callback);

        uv_run(storj_env->loop, UV_RUN_DEFAULT);

        storj_destroy_env(storj_env);
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_io_storj_libstorj_Storj__1exportKeys(
        JNIEnv *env,
        jobject /* instance */,
        jstring location_,
        jstring passphrase_) {
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
