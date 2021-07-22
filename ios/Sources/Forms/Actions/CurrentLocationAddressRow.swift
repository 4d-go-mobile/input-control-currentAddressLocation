//
//  CurrentLocationAddressRow.swift
//  ___PACKAGENAME___
//
//  Created by ___FULLUSERNAME___ on ___DATE___
//  ___COPYRIGHT___
//

import UIKit
import MapKit
import CoreLocation

import Eureka
import QMobileUI

// name of the format
fileprivate let kCurrentLocationAddress = "currentLocationAddress"

// Create an Eureka row for the format
final class CurrentLocationAddressRow: AreaRow<CurrentLocationAddressCell>, RowType {

    required public init(tag: String?) {
        super.init(tag: tag)
    }

}

// Create the associated row cell
open class CurrentLocationAddressCell: TextAreaCell, CLLocationManagerDelegate {
    let locationManager = CLLocationManager()
    
    required public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
    }

    required public init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    open override func setup() {
        super.setup()
        self.isUserInteractionEnabled = false
        locationManager.requestWhenInUseAuthorization()
        if CLLocationManager.locationServicesEnabled() {
            self.locationManager.delegate = self
            self.locationManager.desiredAccuracy = kCLLocationAccuracyBest
            self.locationManager.requestLocation()
        }
    }

    open override func update() {
        self.textView.font = .italicSystemFont(ofSize: self.textView.font?.pointSize ?? 12)
    }
 
    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = manager.location ?? locations.first {

            let ceo: CLGeocoder = CLGeocoder()
            ceo.reverseGeocodeLocation(location) { [weak self] (placemarks, error) in
                var value = ""
                if let error = error {
                    logger.warning("\(error)")
                } else if let places = placemarks, let place = places.first {
                    let placeInfos = [place.subLocality, place.thoroughfare, place.locality, place.country, place.postalCode]
                    let placeInfo = placeInfos.compactMap({$0}).joined(separator: ", ")
                    value = "\(placeInfo)\n"
                    let locValue = location.coordinate
                    if locValue.latitude>0 {
                        value+="+\(locValue.latitude)"
                    } else {
                        value+="\(locValue.latitude)"
                    }
                    if locValue.longitude>0 {
                        value+="+\(locValue.longitude)"
                    } else {
                        value+="\(locValue.longitude)"
                    }
                    self?.row.value = value
                    self?.textView.text = value
                }
            }
        }
    }

    public func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        logger.warning("\(error)")
        self.row.value = ""
        self.textView.text = ""
    }

}

@objc(CurrentLocationAddressRowService)
class CurrentLocationAddressRowService: NSObject, ApplicationService, ActionParameterCustomFormatRowBuilder {
    @objc static var instance: CurrentLocationAddressRowService = CurrentLocationAddressRowService()
    override init() {}
    func buildActionParameterCustomFormatRow(key: String, format: String, onRowEvent eventCallback: @escaping OnRowEventCallback) -> ActionParameterCustomFormatRowType? {
        if format == kCurrentLocationAddress {
            return CurrentLocationAddressRow(key).onRowEvent(eventCallback)
        }
        return nil
    }
}
