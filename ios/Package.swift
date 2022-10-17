// swift-tools-version: 5.7
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "ios",
    platforms: [
        .macOS(.v10_14), .iOS(.v16)
    ],
    products: [
        .library(  name: "ios", targets: ["ios"]),
    ],
    dependencies: [
        .package(url: "https://github.com/4d-for-ios/QMobileUI.git", revision: "HEAD"),
        .package(url: "https://github.com/xmartlabs/Eureka.git", revision: "HEAD")
    ],
    targets: [
        .target(
            name: "ios",
            dependencies: ["QMobileUI", "Eureka"],
            path: "Sources"),
    ]
)
