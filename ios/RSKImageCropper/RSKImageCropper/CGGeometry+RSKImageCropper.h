//
// CGGeometry+RSKImageCropper.h
//
// Copyright (c) 2015 Ruslan Skorb, http://ruslanskorb.com/
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//

#import <CoreGraphics/CoreGraphics.h>
#import <tgmath.h>

// tgmath functions aren't used on iOS when modules are enabled.
// Open Radar - http://www.openradar.me/16744288
// Work around this by redeclaring things here.

#undef cos
#define cos(__x) __tg_cos(__tg_promote1((__x))(__x))

#undef sin
#define sin(__x) __tg_sin(__tg_promote1((__x))(__x))

#undef atan2
#define atan2(__x, __y) __tg_atan2(__tg_promote2((__x), (__y))(__x), \
__tg_promote2((__x), (__y))(__y))

#undef pow
#define pow(__x, __y) __tg_pow(__tg_promote2((__x), (__y))(__x), \
__tg_promote2((__x), (__y))(__y))

#undef sqrt
#define sqrt(__x) __tg_sqrt(__tg_promote1((__x))(__x))

#undef fabs
#define fabs(__x) __tg_fabs(__tg_promote1((__x))(__x))

#undef ceil
#define ceil(__x) __tg_ceil(__tg_promote1((__x))(__x))

#undef floor
#define floor(__x) __tg_floor(__tg_promote1((__x))(__x))

#undef round
#define round(__x) __tg_round(__tg_promote1((__x))(__x))

#ifdef CGFLOAT_IS_DOUBLE
    #define RSK_EPSILON DBL_EPSILON
    #define RSK_MIN DBL_MIN
#else
    #define RSK_EPSILON FLT_EPSILON
    #define RSK_MIN FLT_MIN
#endif

// Line segments.
struct RSKLineSegment {
    CGPoint start;
    CGPoint end;
};
typedef struct RSKLineSegment RSKLineSegment;

// The "empty" point. This is the point returned when, for example, we
// intersect two disjoint line segments. Note that the null point is not the
// same as the zero point.
CG_EXTERN const CGPoint RSKPointNull;

// Returns the exact center point of the given rectangle.
CGPoint RSKRectCenterPoint(CGRect rect);

// Returns the `rect` scaled around the `point` by `sx` and `sy`.
CGRect RSKRectScaleAroundPoint(CGRect rect, CGPoint point, CGFloat sx, CGFloat sy);

// Returns true if `point' is the null point, false otherwise.
bool RSKPointIsNull(CGPoint point);

// Returns the `point` rotated around the `pivot` by `angle`.
CGPoint RSKPointRotateAroundPoint(CGPoint point, CGPoint pivot, CGFloat angle);

// Returns the distance between two points.
CGFloat RSKPointDistance(CGPoint p1, CGPoint p2);

// Make a line segment from two points `start` and `end`.
RSKLineSegment RSKLineSegmentMake(CGPoint start, CGPoint end);

// Returns the line segment rotated around the `pivot` by `angle`.
RSKLineSegment RSKLineSegmentRotateAroundPoint(RSKLineSegment lineSegment, CGPoint pivot, CGFloat angle);

// Returns the intersection of `ls1' and `ls2'. This may return a null point.
CGPoint RSKLineSegmentIntersection(RSKLineSegment ls1, RSKLineSegment ls2);
