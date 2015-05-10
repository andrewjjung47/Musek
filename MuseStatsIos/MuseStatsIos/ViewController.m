//
//  Interaxon, Inc. 2015
//  MuseStatsIos
//

#import "ViewController.h"


@interface ViewController ()

@end

@implementation ViewController

@synthesize concentrateLabel;


- (void)viewDidLoad {
    [super viewDidLoad];
    self.concentrateText=@"0";
    
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(notify:) name:@"museData" object:nil];
    
  
    
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (void)notify:(NSNotification *)notification {
    NSLog(@"Notified!");
    self.concentrateLabel.text = [notification object];
    
    //do stuff
}
@end
