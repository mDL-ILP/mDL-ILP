//
//  LicenseDetailsTableViewController.swift
//  RDWHolder
//
//  Created by mDL developer account on 17/11/2017.
//  
//

import UIKit

class LicenseDetailsTableViewController: UITableViewController {
    var licensePreview : GetLicensePreviewData!
    
    static let HEADER_SECTION = 0;
    static let OWNER_SECTION = 1;
    static let LICENSE_DETAILS_SECTION = 2;
    static let RESTRICTIONS_SECTION = 3;
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        self.tableView.deselectRow(at: indexPath, animated: true)
    }

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 4
    }
    
    override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return [nil, "Holder", "Categories", "Restrictions"][section]
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch (section) {
        case LicenseDetailsTableViewController.HEADER_SECTION:
            return licensePreview.rowsOf(LicenseHeaderTableViewCell.IDENTIFIER);
        case LicenseDetailsTableViewController.OWNER_SECTION:
            return licensePreview.rowsOf(LicenseOwnerViewCell.IDENTIFIER);
        case LicenseDetailsTableViewController.LICENSE_DETAILS_SECTION:
            return licensePreview.rowsOf(LicenseTypeTableViewCell.IDENTIFIER);
        default:
            return 1;
        }
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        switch (indexPath.section) {
        case LicenseDetailsTableViewController.HEADER_SECTION:
            let cell = tableView.dequeueReusableCell(withIdentifier: LicenseHeaderTableViewCell.IDENTIFIER, for: indexPath) as! LicenseHeaderTableViewCell
            licensePreview.provision(cell, didSelectRowAt: indexPath)
            return cell
        case LicenseDetailsTableViewController.OWNER_SECTION:
            let cell = tableView.dequeueReusableCell(withIdentifier: LicenseOwnerViewCell.IDENTIFIER, for: indexPath) as! LicenseOwnerViewCell
            licensePreview.provision(cell, didSelectRowAt: indexPath)
            return cell
        case LicenseDetailsTableViewController.LICENSE_DETAILS_SECTION:
            let cell = tableView.dequeueReusableCell(withIdentifier: LicenseTypeTableViewCell.IDENTIFIER, for: indexPath) as! LicenseTypeTableViewCell
            licensePreview.provision(cell, didSelectRowAt: indexPath)
            return cell
        default:
            return tableView.dequeueReusableCell(withIdentifier: "ownerDetails", for: indexPath)

        }
    }

    override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return false
    }
}
