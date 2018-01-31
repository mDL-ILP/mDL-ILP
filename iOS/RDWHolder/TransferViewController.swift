//
//  TransferViewController.swift
//  RDWHolder
//
//  Created by mDL developer account on 27/11/2017.
//  
//

import UIKit

class TransferViewController: UIViewController, PermitTransferProtocol, FileManagerProtocol {
    var validCodeTest = NSPredicate(format: "SELF MATCHES %@", "\\d{5}")
    
    @IBOutlet weak var transferCode: UITextField!
    @IBAction func transferClicked(_ sender: Any) {
        let code = transferCode.text ?? ""
        if (!validCodeTest.evaluate(with: code)) {
            showAlertWithTitle(title: "Invalid transfer code", message: "Transfer code " + code + " is not valid (must be 5 digits)" )
            return
        }
        
        let transferMessage = PermitTransferMessage();
        transferMessage.deviceToken = UserDefaults.standard.value(forKey: SharedApplicationConstants.firebase_token) as? String
        transferMessage.transferId = code
        NetworkClient.permitTransfer(delegate: self, message: transferMessage)
    }
    
    func onSuccesfullyTransferred() {
        FileUtil.deleteLicense(delegate: self)
    }
    
    func onSuccessfullyDeletedLicense() {
        showAlertWithTitle(
            title: "Transfer successful",
            message: "Transfer successful",
            handler: self.handlerToRegisterPage)
    }
    
    private func handlerToRegisterPage (botton : UIAlertAction) -> Void{
        performSegue(withIdentifier: SegueIdentifier.registerIdentifier, sender: nil)
    }
    
    func onError(message: String) {
        showAlertWithTitle(title: "Transfer not successful", message: message )
    }
    
    private func showAlertWithTitle( title:String, message:String, handler: ((UIAlertAction) -> Void)? = nil ) {
        let alertVC = UIAlertController(title: title, message: message, preferredStyle: .alert)
        
        let okAction = UIAlertAction(title: "Ok", style: .default, handler: handler)
        alertVC.addAction(okAction)
        
        DispatchQueue.main.async  { () -> Void in
            self.present(alertVC, animated: true, completion: nil)
        }
    }
    
    func onSuccessfullySavedLicense() {
        // not relevant
    }
    
}
