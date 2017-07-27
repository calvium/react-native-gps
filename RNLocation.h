// handle the RN 0.39 - 0.40 import breaking change
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#elif __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#else
#import "React/RCTBridgeModule.h"
#endif

@interface RNLocation : NSObject <RCTBridgeModule>

@end
