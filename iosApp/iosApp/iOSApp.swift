import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        registerNativePlayerEngine()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    ExternalUriHandler.shared.onNewUri(uri: url.absoluteString)
                }
        }
    }
}