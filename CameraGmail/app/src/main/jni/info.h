#ifndef __INFO_HEADER__
#define __INFO_HEADER__
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

const uint8_t *get_raw_key();
char *get_account();
char *get_mail();
const uint8_t *get_encrypt_passwd(int *len);

#ifdef __cplusplus
}
#endif

#endif
