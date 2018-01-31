//
//  LoginViewController.swift
//  RDWHolder
//
//
//  
//

import UIKit
import LocalAuthentication

class LoginViewController: UIViewController {

    @IBOutlet weak var bLogin: UIButton!
    @IBOutlet weak var bTouchId: UIButton!
    override func viewDidLoad() {
        super.viewDidLoad()
        //Round the corner of the login button
        bLogin.layer.cornerRadius = 8
        //ROund the corner of the TouchId button
        bTouchId.layer.cornerRadius = 8
        self.hideKeyboardWhenTappedAround()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func login(_ sender: UIButton) {
        self.navigateToMainViewController()
    }
    
    @IBAction func showTouchId(_ sender: UIButton) {
        // 1. Create a authentication context
        let authenticationContext = LAContext()
        var error:NSError?
        
        // 2. Check if the device has a fingerprint sensor
        // If not, show the user an alert view and bail out!
        let hasFingerPrintSensor = authenticationContext.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
        if (hasFingerPrintSensor) {
            startFingerprint(authenticationContext: authenticationContext)
        }else{
         showAlertViewAfterEvaluatingPolicyWithMessage(message: "This device does not have a TouchID sensor")
        }
    }
    
    private func startFingerprint(authenticationContext : LAContext) {
        authenticationContext.evaluatePolicy(
            .deviceOwnerAuthenticationWithBiometrics,
            localizedReason: "RWD authentication ...",
            reply: { [unowned self] (success, error) -> Void in
                
                if( success ) {
                    
                    // Fingerprint recognized
                    // Go to view controller
                    self.navigateToMainViewController()
                    
                }else {
                    
                    // Check if there is an error
                    if let error = error {
                        
                        let message = self.errorMessageForLAErrorCode(errorCode: (error as NSError).code )
                        self.showAlertViewAfterEvaluatingPolicyWithMessage(message: message)
                        
                    }
                    
                }
                
        })
        
    }
    private func errorMessageForLAErrorCode( errorCode:Int ) -> String {
        
        var message = ""
        
        switch errorCode {
            
        case LAError.appCancel.rawValue:
            message = "Authentication was cancelled by application"
            
        case LAError.authenticationFailed.rawValue:
            message = "The user failed to provide valid credentials"
            
        case LAError.invalidContext.rawValue:
            message = "The context is invalid"
            
        case LAError.passcodeNotSet.rawValue:
            message = "Passcode is not set on the device"
            
        case LAError.systemCancel.rawValue:
            message = "Authentication was cancelled by the system"
            
        case LAError.touchIDLockout.rawValue:
            message = "Too many failed attempts."
            
        case LAError.touchIDNotAvailable.rawValue:
            message = "TouchID is not available on the device"
            
        case LAError.userCancel.rawValue:
            message = "The user did cancel"
            
        //case LAError.userFallback.rawValue:
          //  message = "The user chose to use the fallback"
            
        default:
            message = "Did not find error code on LAError object"
            
        }
        
        return message
        
    }
    private func navigateToMainViewController () {
        let typeOfLicense = UserDefaults.standard.value(forKey: SharedApplicationConstants.typeOfCertificate)
        if (typeOfLicense != nil) {
            performSegue(withIdentifier: SegueIdentifier.mainIdentifier, sender: self)
        }else{
            performSegue(withIdentifier: SegueIdentifier.loginIdentifier, sender: self)
        }
    }
    
    func showAlertViewAfterEvaluatingPolicyWithMessage(message : String){
        
        showAlertWithTitle(title: "Error", message: message)
        
    }
    
    func showAlertWithTitle( title:String, message:String ) {
        
        let alertVC = UIAlertController(title: title, message: message, preferredStyle: .alert)
        
        let okAction = UIAlertAction(title: "Ok", style: .default, handler: nil)
        alertVC.addAction(okAction)
        
        DispatchQueue.main.async  { () -> Void in
            
            self.present(alertVC, animated: true, completion: nil)
            
        }
    }
}


//To hide the keyboard once clicked on the background
extension UIViewController {
    func hideKeyboardWhenTappedAround() {
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(UIViewController.dismissKeyboard))
        tap.cancelsTouchesInView = false
        view.addGestureRecognizer(tap)
    }
    
    func dismissKeyboard() {
        view.endEditing(true)
    }
}
