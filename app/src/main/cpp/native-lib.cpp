
#include <jni.h>
#include <string>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <ifaddrs.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_myapplication_MainActivity_getIpAddress(
        JNIEnv *env,
        jobject /* this */) {
    struct ifaddrs *ifAddrStruct = nullptr;
    void *tmpAddrPtr = nullptr;
    std::string ipAddress = "Not found";

    getifaddrs(&ifAddrStruct);

    for (struct ifaddrs *ifa = ifAddrStruct; ifa != nullptr; ifa = ifa->ifa_next) {
        if (!ifa->ifa_addr) {
            continue;
        }
        if (ifa->ifa_addr->sa_family == AF_INET) {
            tmpAddrPtr = &((struct sockaddr_in *) ifa->ifa_addr)->sin_addr;
            char addressBuffer[INET_ADDRSTRLEN];
            inet_ntop(AF_INET, tmpAddrPtr, addressBuffer, INET_ADDRSTRLEN);
            if (strcmp(ifa->ifa_name, "lo") != 0) {
                ipAddress = addressBuffer;
                break;
            }
        }
    }

    if (ifAddrStruct != nullptr) freeifaddrs(ifAddrStruct);

    return env->NewStringUTF(ipAddress.c_str());
}