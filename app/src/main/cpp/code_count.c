//
// Created by 陈双超 on 2019-09-07.
//

#include <jni.h>

#include <stdlib.h>
#include <stdio.h>
#include <android/log.h>

// 指定要注册的类，对应完整的java类名
#define BleData_CLASS "com/wansnn/csc/wsbulb/BlueTooth$BleData"


#define LOG_TAG "jnidemo"

// 定义debug信息
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
// 定义error信息
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
// 定义info信息
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

typedef unsigned char   u8;
typedef unsigned short  u16;

#pragma pack(1)

typedef struct
{
    u8 random;//随机数
    u8 len;//实际数据长度
    u16 buf[8];//加密后的数据
}encrypt_buf_struct;

typedef enum
{
    ComformPhone = 'B',
    ComformDevice = 'C',
} app_message_enum;

typedef struct
{
    u8 message;
    u8 data_len;
    u8 message_data[6];
}app_message_struct;


#pragma pack()


u8* APP_data_count(u8 *buf,u8 len);

/*加密*/
u8* encrypt_buf(app_message_struct buf,u8 len);
/*解密*/
u8* decode_buf(encrypt_buf_struct* buf,u8 len);

#define get_timer_random_number() rand()


/*加密
 * 传输最大长度8byte*/
u8 * encrypt_buf(app_message_struct buf,u8 len)
{

    static encrypt_buf_struct encrypt;
    u8 i;
    if(len>8){
        LOGE("encrypt.random===========returned at 0");
        return 0;
    }
    int randomNum =  get_timer_random_number()&0xff;
    while (randomNum == 0) {
        randomNum =  get_timer_random_number()&0xff;
    }
    randomNum = 75;
    encrypt.random = randomNum&0xff;//随机数获取

    encrypt.len = len;
    u8 buff[len];
    buff[0] = buf.message;
    buff[1] = buf.data_len;
    for (int i = 0; i < buf.data_len; ++i) {
        buff[i+2] = buf.message_data[i];
    }
    for(i=0; i<len; i++)
    {
        if(encrypt.random%2)
        {
//            LOGE("encrypt.random===========if called");
            u16 a,b;
            encrypt.buf[i] = encrypt.random+buff[i];
            a = encrypt.buf[i]>>3;
            b = encrypt.buf[i]<<13;
            encrypt.buf[i] = a|b;
        }
        else
        {
//            LOGE("encrypt.random===========else called");
            encrypt.buf[i] = encrypt.random*buff[i];
        }
        LOGE("encrypt.random===========random no: %d",encrypt.random);
    }
    u8 encrypt_bytearray[len*2+2];
    encrypt_bytearray[0] = encrypt.random;
    encrypt_bytearray[1] = encrypt.len;

    for (int i = 0; i < len; ++i) {
        encrypt_bytearray[i*2+2 +1] = (encrypt.buf[i]>>8)&0xff;
        encrypt_bytearray[i*2+2 ] = encrypt.buf[i]&0xff;
//        LOGE("encrypt.buf encrypt_bytearray: %d",encrypt_bytearray[i*2+2 ]);
//        LOGE("encrypt.buf encrypt_bytearray: %d",encrypt_bytearray[i*2+2 +1]);
    }

    return &encrypt_bytearray;
}
/*解密*/
u8* decode_buf(encrypt_buf_struct* buf,u8 len)
{
    static u8 decode[9];
    u8 i;
    if((buf->len+1)*2 == len)
    {
        for(i=0; i<buf->len; i++)
        {
            if(buf->random%2)
            {
                u16 a,b,c;
                a = buf->buf[i]>>13;
                b = buf->buf[i]<<3;
                c = a|b;
                decode[i] = c-buf->random;
            }
            else
            {
                decode[i] = buf->buf[i]/buf->random;
            }
        }
        decode[buf->len] = 0;
        return decode;
    }
    else
        return 0;
}


/*app发来的数据处理*/
u8* APP_data_count(u8 *buf,u8 len)
{
    u8 *data;
    app_message_struct *message_buf;
    data = decode_buf((encrypt_buf_struct*)buf,len);
    if(data)
    {
        message_buf = (app_message_struct*) data;
        LOGE("ComformPhone------messsagebuf->message-----%d",message_buf->message);
        for(int i=0;i<6;i++){
            LOGE("messsagebuf->data-----%d",message_buf->message_data[i]);
        }
        switch(message_buf->message)
        {
            case ComformPhone:
                LOGE("ComformPhone----------------");
                return message_buf->message_data;
                break;
            case ComformDevice:
                LOGE("ComformDevice----------------");
                return message_buf->message_data;
                break;
        }
    }
    else
    {
        printf("data_error\r\n");
    }
}

JNIEXPORT jbyteArray JNICALL
Java_com_wansnn_csc_wsbulb_BlueTooth_BleService_enCodeBleData(JNIEnv *env, jobject thiz, jobject buf, jint length) {
    // TODO: implement enCodeBleData()
    //    C++   ：env->FindClass("java/lang/String")
//    C语言：(*env)->FindClass(env, "java/lang/String")
    //获取jclass的实例
    jclass jcs = (*env)->FindClass(env, "com/wansnn/csc/wsbulb/BlueTooth/BleData");
    jfieldID message_id = (*env)->GetFieldID(env, jcs, "message", "B");
    jfieldID length_id = (*env)->GetFieldID(env, jcs, "length", "B");
    jfieldID fid_arrays = (*env)->GetFieldID(env,jcs,"mData","[B");

    jbyte jmessage = (*env)->GetByteField(env,buf,message_id);
    jbyte jlength = (*env)->GetByteField(env,buf,length_id);

    jbyteArray jdata_arr = (jbyteArray)(*env)->GetObjectField(env,buf,fid_arrays);

    app_message_struct c_buf;
    c_buf.message = jmessage;
    c_buf.data_len = jlength;

    LOGE("jmessage:%d~~~~~~~~~~~~~~jlength:%d",jmessage,jlength);

    if (jdata_arr != NULL){
        //获取数组的长度  Get the length of the array
        jsize arraylen = (*env)->GetArrayLength(env,jdata_arr);
        //获取arrays对象的指针  Get the pointer of the array object
        jbyte* jdata =  (jbyte*)(*env)->GetByteArrayElements(env,jdata_arr,NULL);
        for(int s=0; s<arraylen; s++){
            c_buf.message_data[s] = jdata[s];
            LOGE("%d~~~~~~~~~~~~~~%d",s,jdata[s]);
        }
    }

    u8 *resultPoint = encrypt_buf(c_buf,length);

    u8 resultArray[length*2+2];
    for(int s=0; s<length*2+2; s++){
        resultArray[s] = resultPoint[s];
        LOGE("%d----------------%d",s,resultPoint[s]);
    }


    jbyteArray array = (*env)->NewByteArray(env,length*2+2);
    (*env)->SetByteArrayRegion(env,array,0,length*2+2,resultArray);

    return array;
}

JNIEXPORT jbyteArray JNICALL
Java_com_wansnn_csc_wsbulb_BlueTooth_BleService_deCodeBleData(JNIEnv *env, jobject thiz, jbyteArray buf) {
    // TODO: implement deCodeBleData()
    jsize arraylen = (*env)->GetArrayLength(env,buf);
    jbyte*  byte_array =  (*env)->GetByteArrayElements(env, buf,NULL);
    LOGE("~~~~~~~~~~~~~~jlength:%d",arraylen);

    u8 *resultPoint = APP_data_count(byte_array,arraylen);

    u8 resultArray[6];
    for(int s=0; s<6; s++){
        resultArray[s] = resultPoint[s];
        LOGE("%d--------解析--%d",s,resultPoint[s]);

    }
    jbyteArray array = (*env)->NewByteArray(env,6);
    (*env)->SetByteArrayRegion(env,array,0,6,resultArray);

    return array;
}
