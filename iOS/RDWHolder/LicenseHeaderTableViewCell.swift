//
//  LicenseHeaderTableViewCell.swift
//  RDWHolder
//
//  Created by mDL developer account on 17/11/2017.
//  
//

import UIKit

class LicenseHeaderTableViewCell: UITableViewCell {
    static let IDENTIFIER="detailsHeader";
    @IBOutlet weak var photo: UIImageView!
    @IBOutlet weak var headerTitle: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
