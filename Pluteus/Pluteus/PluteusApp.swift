//
//  PluteusApp.swift
//  Pluteus
//
//  Created by Developer on 2024.
//

import SwiftUI
import SwiftData

@main
struct PluteusApp: App {
    var sharedModelContainer: ModelContainer = {
        let schema = Schema([
            MediaItem.self,
        ])
        let modelConfiguration = ModelConfiguration(schema: schema, isStoredInMemoryOnly: false)

        do {
            return try ModelContainer(for: schema, configurations: [modelConfiguration])
        } catch {
            fatalError("Could not create ModelContainer: \(error)")
        }
    }()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.modelContext, sharedModelContainer.mainContext)
        }
    }
}
