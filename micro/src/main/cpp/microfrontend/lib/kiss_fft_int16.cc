#include <cstdint>

#include "kiss_fft_common.h"

#define FIXED_POINT 16
namespace kissfft_fixed16 {
#include "kiss_fft/kiss_fft.c"
#include "kiss_fft/kiss_fftr.c"
}  // namespace kissfft_fixed16
#undef FIXED_POINT
