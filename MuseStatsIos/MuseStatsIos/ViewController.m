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
    
    self.concentrateLabel.text = self.concentrateText;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
