//
//  SettingsTableViewController.swift
//  RDWHolder
//
//
//  
//

import UIKit

class SettingsTableViewController: UITableViewController, DownloadProtocol, FileManagerProtocol {
    
    
    func onSuccessfullySavedLicense() {
        showAlertWithTitle(title: "Successfull!", message: "Your license been downloaed successfully!", handler: nil)
    }
    
    func onSuccessfullyDeletedLicense() {
        showAlertWithTitle(title: "Removed!", message: "Your license has been deleted", handler: self.handlerToRegisterPage)
    }
    
    private func showAlertWithTitle( title:String, message:String, handler: ((UIAlertAction) -> Void)? = nil ) {
        
        let alertVC = UIAlertController(title: title, message: message, preferredStyle: .alert)
        
        
        let okAction = UIAlertAction(title: "Ok", style: .default, handler: handler)
        alertVC.addAction(okAction)
        
        DispatchQueue.main.async  { () -> Void in
            
            self.present(alertVC, animated: true, completion: nil)
            
        }
    }
    
    func onSuccessfullDownload(certificate : String) {
        UserDefaults.standard.setValue(SharedApplicationConstants.realCertificate, forKey: SharedApplicationConstants.typeOfCertificate)
            save(license: certificate)
    }
    private func save(license : String) {
        FileUtil.saveLicense(delegate : self, license: license)
    }
    func onError(message: String) {
        showAlertWithTitle(title: "Error!", message: "Something wrong happened, check the log!")
    }
    override func viewDidLoad() {
        super.viewDidLoad()

        //To remove empty cells in the bottom of the table view
        self.tableView.tableFooterView = UIView()
        
        //To change the background of the table view
        self.tableView.backgroundColor = UIColor.groupTableViewBackground
        
    }

    

    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 4
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let section = indexPath.section
        let row = indexPath.row
        switch section {
        case 0:
            self.tableView.deselectRow(at: indexPath, animated: true)
            switch row {
            case 0:
                break
            case 1:
                let message = DownloadMessage()
                message.token = UserDefaults.standard.value(forKey: SharedApplicationConstants.firebase_token) as? String
                NetworkClient.download(delegate: self, message: message, fileDelegate : self)
                break
            case 2:
                FileUtil.deleteLicense(delegate : self)
                break;
            case 3:
                performSegue(withIdentifier: SegueIdentifier.logoutIdentifier, sender: nil)
                break
            default:
                break
            }
            break
        default:
            break
        }
    }
    private func handlerToRegisterPage (botton : UIAlertAction) -> Void{
       performSegue(withIdentifier: SegueIdentifier.registerIdentifier, sender: nil)
    }
}
