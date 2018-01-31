//
//  MainTableViewController.swift
//  RDWHolder
//
//
//  
//

import UIKit

class MainTableViewController: UITableViewController, RDWErrorProtocol {
    var licensePreview : GetLicensePreviewData!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false
        
        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        //self.navigationItem.rightBarButtonItem = self.editButtonItem
        
        //To remove empty cells in the bottom of the table view
        self.tableView.tableFooterView = UIView()
        
        //To change the background of the table view
        self.tableView.backgroundColor = UIColor.groupTableViewBackground
        
        self.licensePreview = GetLicensePreviewData()
        self.licensePreview.prepare(callback: self)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: - Table view data source
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        return 2 // set to 3 to enable connection type section
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch section {
        case 0:
            return 1
        case 1:
            return 3
        case 2:
            return 2
        default:
            return 0
        }
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        self.tableView.deselectRow(at: indexPath, animated: true)
        
        let section = indexPath.section
        let row = indexPath.row
        switch section {
        case 0:
            performSegue(withIdentifier: SegueIdentifier.showDetailsSegue, sender: nil)
            break
        case 1:
            switch row {
            case 0:
                performSegue(withIdentifier: SegueIdentifier.qrCodeGenerateSegue, sender: ["0", "QR Code Licence"])
                break
            case 1:
                performSegue(withIdentifier: SegueIdentifier.qrCodeGenerateSegue, sender: ["18", "QR Code +18"])
                break
            case 2:
                performSegue(withIdentifier: SegueIdentifier.qrCodeGenerateSegue, sender: ["21", "QR Code +21"])
                break
            default:
                break
            }
            break
        default:
            break
        }
    }
    
    override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return [nil, "Share License", "Connection Type"][section]
    }
   
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        switch indexPath.section {
        case 0:
            let cell = tableView.dequeueReusableCell(withIdentifier: UserCellViewController.IDENTIFIER, for: indexPath) as! UserCellViewController
            self.licensePreview.provision(cell, didSelectRowAt: indexPath)
            return cell;
        case 1:
            let cell = tableView.dequeueReusableCell(withIdentifier: "viewLicense", for: indexPath) as! ShareCellViewController
            switch indexPath.row {
            case 0:
                cell.label.text = "Full License"
                cell.age = 0
            case 1:
                cell.label.text = "18+"
                cell.age = 18
            case 2:
                cell.label.text = "21+"
                cell.age = 21
            default:
                cell.label.text = ""
                cell.age = 0
            }
            return cell
        case 2:
            let cell = tableView.dequeueReusableCell(withIdentifier: "connectionType", for: indexPath) as! ConnectionCellViewController
            switch indexPath.row {
            case 0:
                cell.label.text = "Bluetooth Low Energy"
                cell.accessoryType = UITableViewCellAccessoryType.checkmark
            case 1:
                cell.label.text = "Internet Download"
                cell.accessoryType = UITableViewCellAccessoryType.none
            default:
                cell.label.text = ""
                cell.accessoryType = UITableViewCellAccessoryType.none
            }
            return cell
        default:
            return tableView.dequeueReusableCell(withIdentifier: "viewLicense", for: indexPath)
        }
    }
    
    
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        let identifier = segue.identifier
        if (identifier == nil) {
            return
        }
        switch identifier! {
        case SegueIdentifier.qrCodeGenerateSegue:
            let destination = segue.destination
            switch destination {
            case is QRCodeGeneratorViewController:
                (destination as! QRCodeGeneratorViewController).type = (sender as! [String])[0]
                destination.title = (sender as! [String])[1]
                break
            default:
                break
            }
            break
        case SegueIdentifier.showDetailsSegue:
            let destination = segue.destination as! LicenseDetailsTableViewController;
            destination.licensePreview = licensePreview;
            break
        default:
            break
        }
    }
    
    func onError(message: String) {
        showAlertWithTitle(title: "Could not load license data", message: message)
    }
    
    private func showAlertWithTitle( title:String, message:String, handler: ((UIAlertAction) -> Void)? = nil ) {
        let alertVC = UIAlertController(title: title, message: message, preferredStyle: .alert)

        let okAction = UIAlertAction(title: "Ok", style: .default, handler: handler)
        alertVC.addAction(okAction)
        
        DispatchQueue.main.async  { () -> Void in
            
            self.present(alertVC, animated: true, completion: nil)
            
        }
    }
}
