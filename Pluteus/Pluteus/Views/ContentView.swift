//
//  ContentView.swift
//  Pluteus
//
//  Created by Developer on 2024.
//

import SwiftUI
import SwiftData

struct ContentView: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: ContentViewModel!
    @State private var showingAddSheet = false
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Search and Filters
                filtersSection
                
                // List
                if viewModel.filteredItems.isEmpty {
                    emptyStateView
                } else {
                    mediaListView
                }
            }
            .navigationTitle("Медиатека")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { showingAddSheet = true }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showingAddSheet) {
                AddMediaView()
                    .environment(\.modelContext, modelContext)
            }
        }
        .onAppear {
            viewModel = ContentViewModel(modelContext: modelContext)
        }
    }
    
    private var filtersSection: some View {
        VStack(spacing: 8) {
            // Search
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.gray)
                TextField("Поиск по названию...", text: $viewModel.searchQuery)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .onChange(of: viewModel.searchQuery) { _, _ in
                        viewModel.applyFiltersAndSort()
                    }
                
                if !viewModel.searchQuery.isEmpty {
                    Button(action: {
                        viewModel.searchQuery = ""
                        viewModel.applyFiltersAndSort()
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.gray)
                    }
                }
            }
            .padding(.horizontal)
            .padding(.top, 8)
            
            // Type Filter
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    FilterChip(title: "Все", isSelected: viewModel.selectedTypeFilter == nil) {
                        viewModel.selectedTypeFilter = nil
                        viewModel.applyFiltersAndSort()
                    }
                    
                    ForEach(MediaType.allCases, id: \.self) { type in
                        FilterChip(title: type.displayName, isSelected: viewModel.selectedTypeFilter == type) {
                            viewModel.selectedTypeFilter = type
                            viewModel.applyFiltersAndSort()
                        }
                    }
                }
                .padding(.horizontal)
            }
            
            // Status Filter
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    FilterChip(title: "Все статусы", isSelected: viewModel.selectedStatusFilter == nil) {
                        viewModel.selectedStatusFilter = nil
                        viewModel.applyFiltersAndSort()
                    }
                    
                    ForEach(MediaStatus.allCases, id: \.self) { status in
                        FilterChip(title: status.displayTitle, isSelected: viewModel.selectedStatusFilter == status) {
                            viewModel.selectedStatusFilter = status
                            viewModel.applyFiltersAndSort()
                        }
                    }
                }
                .padding(.horizontal)
            }
            
            // Sort
            HStack {
                Text("Сортировка:")
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                Picker("", selection: $viewModel.sortOption) {
                    ForEach(ContentViewModel.SortOption.allCases, id: \.self) { option in
                        Text(option.rawValue).tag(option)
                    }
                }
                .pickerStyle(MenuPickerStyle())
                .onChange(of: viewModel.sortOption) { _, _ in
                    viewModel.applyFiltersAndSort()
                }
            }
            .padding(.horizontal)
            .padding(.bottom, 8)
        }
        .background(Color(.systemGroupedBackground))
    }
    
    private var mediaListView: some View {
        List(viewModel.filteredItems) { item in
            NavigationLink(destination: DetailView(item: item)) {
                MediaRowView(item: item)
            }
            .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                Button(role: .destructive) {
                    viewModel.deleteItem(item)
                } label: {
                    Label("Удалить", systemImage: "trash")
                }
                
                Button {
                    let nextStatus = getNextStatus(for: item)
                    viewModel.updateStatus(item, to: nextStatus)
                } label: {
                    Label("Статус", systemImage: "arrow.triangle.2.circlepath")
                }
                .tint(.blue)
            }
        }
        .listStyle(InsetGroupedListStyle())
    }
    
    private func getNextStatus(for item: MediaItem) -> MediaStatus {
        let statuses = MediaStatus.statuses(for: item.type)
        guard let currentIndex = statuses.firstIndex(of: item.status) else {
            return .planned
        }
        
        let nextIndex = (currentIndex + 1) % statuses.count
        return statuses[nextIndex]
    }
    
    private var emptyStateView: some View {
        VStack(spacing: 16) {
            Image(systemName: "film")
                .font(.system(size: 60))
                .foregroundColor(.gray.opacity(0.5))
            
            Text("Нет записей")
                .font(.headline)
                .foregroundColor(.secondary)
            
            Text("Добавьте фильм, сериал или книгу\nнажав на кнопку +")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.caption)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(isSelected ? Color.blue : Color(.systemGray5))
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(16)
        }
    }
}

#Preview {
    ContentView()
        .modelContainer(for: MediaItem.self, inMemory: true)
}
