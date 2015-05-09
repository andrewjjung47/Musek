// Copyright 2015 InteraXon, Inc.

#import <Foundation/Foundation.h>
#import "IXNMuseFileWriter.h"

/**
 * Provides API for MuseFileWriter creation
 */
@interface IXNMuseFileWriterFactory : NSObject

/**
 * Creates and returns IXNMuseFileWriter object based on provided path.
 * Interaxon MuseFile implementation is used in this case.
 */
+ (id <IXNMuseFileWriter>)museFileWriterWithPathString:(NSString*)filePath;

@end
