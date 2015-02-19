/*!
 * Project MOST - Moving Outcomes to Standard Telemedicine Practice
 * http://most.crs4.it/
 *
 * Copyright 2014, CRS4 srl. (http://www.crs4.it/)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * See license-GPLv2.txt or license-MIT.txt
 */

import UIKit

class ViewController: UIViewController {

    @IBOutlet var fldUser : UITextField
    @IBOutlet var fldPwd : UITextField
    
    let manager = AFHTTPRequestOperationManager()
    var jsonResult: Dictionary<String, AnyObject> = [:]

    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view, typically from a nib.
        manager.GET( "http://localhost:8000/test",
            parameters: ["access_token" : "b020ce311076f202b5b20d18f46c759858178e23"],
            success: { (operation: AFHTTPRequestOperation!,responseObject: AnyObject!) in
                println("JSON: " + responseObject.description)
                
            },
            failure: { (operation: AFHTTPRequestOperation!,error: NSError!) in
                println("Error: " + error.localizedDescription)
                println("Body: " + operation.responseString)
            })
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    @IBAction func doStep1(sender : AnyObject) {
        
        var parameters = [
            "username":"admin",
            "password":"admin",
            "client_id" : "d72835cfb6120e844e13",
            "client_secret" : "8740cac9a53f2cdd1bded9cfbb60fdb3b5396863",
            "grant_type" : "password"
        ]
        manager.POST( "http://localhost:8000/oauth2/access_token/",
        parameters: parameters,
        success: { (operation: AFHTTPRequestOperation!,responseObject: AnyObject!) in
            println("JSON: " + responseObject.description)

            var jsonResult = responseObject as Dictionary<String, AnyObject>
            self.jsonResult = jsonResult
            
        },
        failure: { (operation: AFHTTPRequestOperation!,error: NSError!) in
            println("Error: " + error.localizedDescription)
            println("Body: " + operation.responseString)
            
        })
    }
    @IBAction func doStep2(sender : AnyObject) {

        var token : AnyObject? = self.jsonResult["access_token"]
        manager.GET( "http://localhost:8000/test",
            parameters: ["access_token" : token as String],
            success: { (operation: AFHTTPRequestOperation!,responseObject: AnyObject!) in
                println("JSON: " + responseObject.description)
                
            },
            failure: { (operation: AFHTTPRequestOperation!,error: NSError!) in
                println("Error: " + error.localizedDescription)
                println("Body: " + operation.responseString)
            })
    }
}

