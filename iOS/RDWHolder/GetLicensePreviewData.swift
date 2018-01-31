//
//  GetLicensePreviewData.swift
//  RDWHolder
//
//  Created by mDL developer account on 06/11/2017.
//  
//

import Foundation
import UIKit
import BerTlv

class GetLicensePreviewData : FileManagerProtocol {
    var certificate: Certificate!
    
    var issuingState: String! = ""
    var surName: String! = "[error loading license]"
    var otherName: String! = ""
    var birthDate: String! = ""
    var birthPlace: String! = ""
    var issuingDate: String! = ""
    var expiryDate: String! = ""
    var issuingAuthority: String! = ""
    var documentNumber: String! = ""
    var categories: [LicenseCategory]! = []
    var imageData: Data! = Data()
    
    func prepare(callback: RDWErrorProtocol) {
        let certificateString = FileUtil.readLicense(delegate: self)
        if (certificateString == nil) {
            callback.onError(message: "No license data found")
            return
        }
        let certificateLoader = DemoCertificateLoader()
        let maybeCertificate = certificateLoader.loadCertificate(certificate: certificateString!)
        if (maybeCertificate == nil) {
            callback.onError(message: "Could not parse license data")
            return
        }
        self.certificate = maybeCertificate!
        
        
        let parser = BerTlvParser()
        let parsedDG1 = parser.parseConstructed(self.certificate.dataGroups["0001"]!.dataArray())!
        let parsedDetails = parser.parseTlvs(parsedDG1.find(BerTag(0x5F, secondByte: 0x02))!.value)!
        
        // 46 is based on Table 13 in DA33LDS
        imageData = Data(Array(parser.parseConstructed(self.certificate.dataGroups["0006"]!.dataArray())!
            .find(BerTag(0x5F, secondByte: 0x2E)).value[46...]));

        issuingState = parsedDetails.find(BerTag(0x5F, secondByte: 0x03))!.textValue()
        surName = parsedDetails.find(BerTag(0x5F, secondByte: 0x04))!.textValue()
        otherName = parsedDetails.find(BerTag(0x5F, secondByte: 0x05))!.textValue()
        birthDate = Util.bcdToDateString(bcd: parsedDetails.find(BerTag(0x5F, secondByte: 0x06))!.value)
        birthPlace = parsedDetails.find(BerTag(0x5F, secondByte: 0x07))!.textValue()
        issuingDate = Util.bcdToDateString(bcd: parsedDetails.find(BerTag(0x5F, secondByte: 0x0A))!.value)
        expiryDate = Util.bcdToDateString(bcd: parsedDetails.find(BerTag(0x5F, secondByte: 0x0B))!.value)
        issuingAuthority = parsedDetails.find(BerTag(0x5F, secondByte: 0x0C))!.textValue()
        documentNumber = parsedDetails.find(BerTag(0x5F, secondByte: 0x0E))!.textValue()
        
        categories = parsedDG1.findAll(BerTag(0x87)).map(toCategory)
    }
    
    func toCategory(data: Any) -> LicenseCategory {
        return LicenseCategory(mdlData: (data as! BerTlv).value)
    }
    
    func onSuccessfullySavedLicense() {
    }
    
    func onSuccessfullyDeletedLicense() {
    }
    
    func onError(message: String) {
        print(message)
    }
    
    func provision(_ viewCell: UserCellViewController, didSelectRowAt indexPath: IndexPath) {
        viewCell.userName.text = otherName + " " + surName
        viewCell.picture.image = UIImage(data: imageData)
    }
    
    func rowsOf(_ type: String) -> Int {
        switch (type) {
        case LicenseHeaderTableViewCell.IDENTIFIER:
            return 1
        case LicenseOwnerViewCell.IDENTIFIER:
            return 8
        case LicenseTypeTableViewCell.IDENTIFIER:
            return categories.count
        default:
            return 0;
        }
    }
    
    func provision(_ viewCell: LicenseHeaderTableViewCell, didSelectRowAt indexPath: IndexPath) {
        if (indexPath.row == 0) {
            viewCell.photo.image = UIImage(data: imageData);
            viewCell.headerTitle.text = "Drivers License";
        }
    }
    
    func provision(_ viewCell: LicenseOwnerViewCell, didSelectRowAt indexPath: IndexPath) {
        switch(indexPath.row) {
        case 0:
            viewCell.title.text = "Last name"
            viewCell.detail.text = surName
        case 1:
            viewCell.title.text = "First name"
            viewCell.detail.text = otherName
        case 2:
            viewCell.title.text = "Date of birth"
            viewCell.detail.text = birthDate
        case 3:
            viewCell.title.text = "Birthplace"
            viewCell.detail.text = birthPlace
        case 4:
            viewCell.title.text = "Valid from"
            viewCell.detail.text = issuingDate
        case 5:
            viewCell.title.text = "Valid to"
            viewCell.detail.text = expiryDate
        case 6:
            viewCell.title.text = "Issued by"
            viewCell.detail.text = issuingAuthority
        case 7:
            viewCell.title.text = "License number"
            viewCell.detail.text = documentNumber
        default:
            viewCell.title.text = "[empty]"
            viewCell.detail.text = "[empty]"
        }
    }

    func provision(_ viewCell: LicenseTypeTableViewCell, didSelectRowAt indexPath: IndexPath) {
        let category = categories[indexPath.row]
        viewCell.title.text = category.licenseType
        viewCell.logo.image = UIImage(named: "icon_" + category.licenseType.lowercased())
        viewCell.dateFrom.text =  "Valid from: " + category.fromDate
        viewCell.dateTo.text = "Valid to: " + category.toDate
    }
}
