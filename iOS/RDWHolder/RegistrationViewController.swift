//
//  RegistrationViewController.swift
//  RDWHolder
//
//
//  
//

import UIKit
import CoreData
import Firebase
import FirebaseInstanceID
import UserNotifications

class RegistrationViewController: UIViewController, RegistrationProtocol, TransferRequesterProtocol, MessagingDelegate, DownloadProtocol, FileManagerProtocol{
    @IBOutlet weak var bRegister: UIButton!
    
    @IBAction func register(_ sender: UIButton) {
          super.viewDidLoad()
         let message = RegistrationMessage()
        message.deviceToken = UserDefaults.standard.value(forKey: SharedApplicationConstants.firebase_token) as? String
        message.deviceDescription = "Device description"
        message.publicKey = CryptographyUtil.getBase64StringOfPublicKey(key: CryptographyUtil.getPublicKeyAndPrivateKeyForRSA()![0])
        NetworkClient.register(delegate: self, message: message)
    }
    @IBOutlet weak var bRequestEntollmentId: UIButton!
    func onSuccessfullyDeletedLicense() {
        //Not implemented and not needed
    }
    
    func onSuccessfullySavedLicense() {
        performSegue(withIdentifier: SegueIdentifier.mainIdentifier, sender: self)
    }
    
    func onSuccessfullDownload(certificate : String) {
        UserDefaults.standard.setValue(SharedApplicationConstants.realCertificate, forKey: SharedApplicationConstants.typeOfCertificate)
        save(license: certificate)
        performSegue(withIdentifier: SegueIdentifier.mainIdentifier, sender: nil)
    }
    
    // [START refresh_token]
    
    // [END refresh_token]
    // [START ios_10_data_message]
    // Receive data messages on iOS 10+ directly from FCM (bypassing APNs) when the app is in the foreground.
    // To enable direct data messages, you can set Messaging.messaging().shouldEstablishDirectChannel to true.
    func messaging(_ messaging: Messaging, didReceive remoteMessage: MessagingRemoteMessage) {
        print("Received data message: \(remoteMessage.appData)")
        messaging.shouldEstablishDirectChannel = true
        let download = remoteMessage.appData["message"] as? String
        if (download != nil) {
            if(download! == "Download") {
                let message = DownloadMessage()
                message.token = UserDefaults.standard.value(forKey: SharedApplicationConstants.firebase_token) as? String
                NetworkClient.download(delegate: self, message: message, fileDelegate: self)
            }
        }
    }
    // [END ios_10_data_message]
    
    func messaging(_ messaging: Messaging, didRefreshRegistrationToken fcmToken: String) {
        UserDefaults.standard.setValue(fcmToken, forKey: SharedApplicationConstants.firebase_token)
    }
    
    
   let gcmMessageIDKey = "gcm.message_id"
    
    @IBOutlet weak var lEnrollmentId: UILabel!
    func onTransferRequestIdReceived(id : String) {
        lEnrollmentId.text = id
    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        lEnrollmentId.text = ""
        configureFirebase()
    }
    
    private func configureFirebase() {
        
        Messaging.messaging().delegate = self
        
    }
    
    override func viewWillAppear(_ animated: Bool) {
        let register = UserDefaults.standard.value(forKey: SharedApplicationConstants.registered)
        if (register == nil) {
            bRegister.isHidden = false
            bRequestEntollmentId.isHidden = true
        }else{
            bRegister.isHidden = true
            bRequestEntollmentId.isHidden = false
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    func onSuccessfullyRegistered() {
        Util.printValue("Registration done successfully")
        UserDefaults.standard.setValue("true", forKey: SharedApplicationConstants.registered)
        self.bRegister.isHidden = true
        self.bRequestEntollmentId.isHidden = false
    }
    
    func onError(message : String) {
        print(message)
        showAlertWithTitle(title: "Error", message: "Something wrong happened, check the log!")
        
    }
    @IBAction func requestEnrollmentId(_ sender: UIButton) {
        let message = RequestTransferMessage()
        message.token = UserDefaults.standard.value(forKey: SharedApplicationConstants.firebase_token) as? String
        NetworkClient.requestTransferId(delegate: self, message: message)
    }
    private func showAlertWithTitle( title:String, message:String, handler: ((UIAlertAction) -> Void)? = nil ) {
        
        let alertVC = UIAlertController(title: title, message: message, preferredStyle: .alert)
        
        
        let okAction = UIAlertAction(title: "Ok", style: .default, handler: handler)
        alertVC.addAction(okAction)
        
        DispatchQueue.main.async  { () -> Void in
            
            self.present(alertVC, animated: true, completion: nil)
            
        }
    }
    @IBAction func UseExampleLicense(_ sender: UIButton) {
        let license = DemoCertificateLoader.loadExampleCertificate()
        UserDefaults.standard.setValue(SharedApplicationConstants.DemoCertificate, forKey: SharedApplicationConstants.typeOfCertificate)
        save(license: license)
    }
    
    private func save(license : String) {
        FileUtil.saveLicense(delegate : self, license: license)
    }
    
}


// [START ios_10_message_handling]
@available(iOS 10, *)
extension RegistrationViewController : UNUserNotificationCenterDelegate {
    
    // Receive displayed notifications for iOS 10 devices.
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let userInfo = notification.request.content.userInfo
        
        // With swizzling disabled you must let Messaging know about the message, for Analytics
        // Messaging.messaging().appDidReceiveMessage(userInfo)
        // Print message ID.
        if let messageID = userInfo[gcmMessageIDKey] {
            print("Message ID: \(messageID)")
        }
        
        // Print full message.
        print(userInfo)
        
        // Change this to your preferred presentation option
        completionHandler([])
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        // Print message ID.
        if let messageID = userInfo[gcmMessageIDKey] {
            print("Message ID: \(messageID)")
        }
        
        // Print full message.
        print(userInfo)
        
        completionHandler()
    }
}
// [END ios_10_message_handling]




