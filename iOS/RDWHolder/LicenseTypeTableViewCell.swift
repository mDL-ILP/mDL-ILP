//
//  LicenseTypeTableViewCell.swift
//  RDWHolder
//
//  Created by mDL developer account on 17/11/2017.
//  
//

import UIKit

class LicenseTypeTableViewCell: UITableViewCell {
    static let IDENTIFIER = "licenseDetails"
    
    @IBOutlet weak var title: UILabel!
    @IBOutlet weak var logo: UIImageView!
    @IBOutlet weak var dateFrom: UILabel!
    @IBOutlet weak var dateTo: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
}
