#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "SFMT.h"

int main(int argc, char** argv){
  int i;
  uint32_t *array32;
  uint64_t *array64;
  sfmt_t sfmt;
  int count = 1;
  int buffer_size = count;
  int seed = 0;
  int bit = 32;
  int each = 0;
  uint32_t* seed_array = NULL;
  int seed_array_size = 0;

  for(i = 0; i<argc; i++){
    if(strcmp(argv[i], "-n") == 0 && i+1 < argc){
      count = atoi(argv[i+1]);
      i ++;
    } else if((strcmp(argv[i], "-s") == 0 || strcmp(argv[i], "--seed") == 0) && i+1<argc){
      seed = atoi(argv[i+1]);
      i ++;
    } else if(strcmp(argv[i], "--seed-array") == 0 && i+1<argc){
      seed_array = (uint32_t*)malloc(sizeof(uint32_t) * 4);
      seed_array[0] = 0x01234567;
      seed_array[1] = 0x89ABCDEF;
      seed_array[2] = 0xFEDCBA98;
      seed_array[3] = 0x76543210;
      seed_array_size = 4;
    } else if ((strcmp(argv[i], "-b") == 0 || strcmp(argv[i], "--bit") == 0) && i + 1 < argc) {
		bit = atoi(argv[i + 1]);
		if (bit != 32 && bit != 64) {
			printf("ERROR: -b should be 32 or 64");
			exit(EXIT_FAILURE);
		}
		i++;
	}
	else if ((strcmp(argv[i], "-e") == 0 || strcmp(argv[i], "--each") == 0)) {
		each = 1;
	}
	else {
		printf("ERROR: unknown option '%s'\n", argv[i]);
	}
  }

  buffer_size = count;
  if(buffer_size < sfmt_get_min_array_size32(&sfmt)){
    buffer_size = sfmt_get_min_array_size32(&sfmt);
  }

  buffer_size = buffer_size / 4 * 4;

  if (bit == 32 && sfmt_get_min_array_size32(&sfmt) > buffer_size) {
    printf("buffer size too small!: %d/%d\n", buffer_size, sfmt_get_min_array_size32(&sfmt));
    exit(EXIT_FAILURE);
  }
  if (bit == 64 && sfmt_get_min_array_size64(&sfmt) > buffer_size) {
	  printf("buffer size too small!: %d/%d\n", buffer_size, sfmt_get_min_array_size64(&sfmt));
	  exit(EXIT_FAILURE);
  }
  printf("%s\n%d bit generated randoms\n", sfmt_get_idstring(&sfmt), bit);

  if(seed_array == NULL){
    sfmt_init_gen_rand(&sfmt, seed);
  } else {
    sfmt_init_by_array(&sfmt, seed_array, seed_array_size);
  }
  if (each) {
	if (bit == 32) {
		/* 32 bit generation */
		for (i = 0; i < count; i++) {
			printf("%u\n", sfmt_genrand_uint32(&sfmt));
		}
	}
	else if (bit == 64) {
		/* 64 bit generation */
		for (i = 0; i < count; i++) {
			printf("%lu\n", sfmt_genrand_uint64(&sfmt));
		}
	}
  } else if (bit == 32) {
	  /* 32 bit generation */
	  array32 = (uint32_t*)malloc(sizeof(uint32_t) * buffer_size);
	  sfmt_fill_array32(&sfmt, array32, buffer_size);
	  for (i = 0; i < count; i++) {
		  printf("%u\n", array32[i]);
	  }
	  free(array32);
  }
  else if (bit == 64) {
	  /* 64 bit generation */
	  array64 = (uint64_t*)malloc(sizeof(uint64_t) * buffer_size);
	  sfmt_fill_array64(&sfmt, array64, buffer_size);
	  for (i = 0; i < count; i++) {
		  printf("%lu\n", array64[i]);
	  }
	  free(array64);
  }
  if(seed_array != NULL){
    free(seed_array);
  }

  return EXIT_SUCCESS;
}
