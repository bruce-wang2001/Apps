#include "info.h"
#include <string.h>
#include <stdlib.h>

const uint8_t *get_raw_key()
{
    static uint8_t raw_key[16];
    raw_key[0] = 0xd5;
    raw_key[1] = 0x4c;
    raw_key[2] = 0x8c;
    raw_key[3] = 0xf5;
    raw_key[4] = 0x29;
    raw_key[5] = 0xe;
    raw_key[6] = 0xdf;
    raw_key[7] = 0xf3;
    raw_key[8] = 0xae;
    raw_key[9] = 0x99;
    raw_key[10] = 0x23;
    raw_key[11] = 0xa0;
    raw_key[12] = 0xc1;
    raw_key[13] = 0xf5;
    raw_key[14] = 0xea;
    raw_key[15] = 0x80;
    return raw_key;
}

char *get_account()
{
    char account[18];
    char *buf = (char *)malloc(sizeof(account));
    account[0] = 's';
    account[1] = 'e';
    account[2] = 'c';
    account[3] = 'u';
    account[4] = 'r';
    account[5] = 'i';
    account[6] = 't';
    account[7] = 'y';
    account[8] = '.';
    account[9] = 't';
    account[10] = 'e';
    account[11] = 's';
    account[12] = 't';
    account[13] = 't';
    account[14] = 'e';
    account[15] = 's';
    account[16] = 't';
    account[17] = '\0';
    memcpy(buf, account, sizeof(account));
    return buf;
}

char *get_mail()
{
    char mail[28];
    char *buf = (char *)malloc(sizeof(mail));

    mail[0] = 's';
    mail[1] = 'e';
    mail[2] = 'c';
    mail[3] = 'u';
    mail[4] = 'r';
    mail[5] = 'i';
    mail[6] = 't';
    mail[7] = 'y';
    mail[8] = '.';
    mail[9] = 't';
    mail[10] = 'e';
    mail[11] = 's';
    mail[12] = 't';
    mail[13] = 't';
    mail[14] = 'e';
    mail[15] = 's';
    mail[16] = 't';
    mail[17] = '@';
    mail[18] = 'g';
    mail[19] = 'm';
    mail[20] = 'a';
    mail[21] = 'i';
    mail[22] = 'l';
    mail[23] = '.';
    mail[24] = 'c';
    mail[25] = 'o';
    mail[26] = 'm';
    mail[27] = '\0';
    memcpy(buf, mail, sizeof(mail));
    return buf;
}

const uint8_t *get_encrypt_passwd(int *len)
{
    static uint8_t encrypted[] = {
0xe,
0xc8,
0x9c,
0xa3,
0x8,
0x89,
0x6,
0xfe,
0xb,
0xc7,
0x80,
0x7a,
0x6a,
0xbe,
0x29,
0xd2
    };
    *len = sizeof(encrypted);
    return encrypted;
}