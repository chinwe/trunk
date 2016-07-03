//
//  ViewController.swift
//  Calculator
//
//  Created by 张俊伟 on 16/7/3.
//  Copyright © 2016年 张俊伟. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    @IBOutlet weak var display: UILabel!
    

    @IBAction func appendDigital(sender: UIButton) {
        
        display.textColor = UIColor.redColor()
    }
    
    
}

