
CC=gcc
CFLAGS=-c -Wall -fPIC -I/usr/lib/jvm/java/include -I/usr/lib/jvm/java/include/linux
LDFLAGS=-fPIC -shared -lstorj

SOURCES_DIR=src/main/cpp
OBJECTS_DIR=target/cpp
EXECUTABLE=target/classes/libstorj-java.so

SOURCES=$(shell find '$(SOURCES_DIR)' -type f -name '*.cpp')
OBJECTS=$(SOURCES:$(SOURCES_DIR)/%.cpp=$(OBJECTS_DIR)/%.o)

all: $(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

$(OBJECTS): $(SOURCES)
	mkdir -p $(OBJECTS_DIR)
	$(CC) $(CFLAGS) $< -o $@

clean:
	rm -rf $(OBJECTS_DIR) $(EXECUTABLE)