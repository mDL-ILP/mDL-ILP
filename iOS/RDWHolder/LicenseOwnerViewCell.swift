//
//  LicenseOwnerViewCell.swift
//  RDWHolder
//
//  Created by mDL developer account on 17/11/2017.
//  
//

import UIKit

class LicenseOwnerViewCell: UITableViewCell {
    static let IDENTIFIER="ownerDetails";

    @IBOutlet weak var title: UILabel!
    @IBOutlet weak var detail: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
