//
//  AddMediaView.swift
//  Pluteus
//
//  Created by Developer on 2024.
//

import SwiftUI
import SwiftData

struct AddMediaView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel: AddViewModel!
    @State private var showingImagePicker = false
    
    var body: some View {
        NavigationView {
            Form {
                // Type Selection
                Section("Тип контента") {
                    Picker("Тип", selection: $viewModel.selectedType) {
                        ForEach(MediaType.allCases, id: \.self) { type in
                            Text(type.displayName).tag(type)
                        }
                    }
                    .onChange(of: viewModel.selectedType) { _, _ in
                        viewModel.resetForm()
                    }
                }
                
                // Search Section
                Section("Поиск") {
                    HStack {
                        TextField("Название...", text: $viewModel.searchQuery)
                            .autocorrectionDisabled()
                        
                        Button(action: {
                            Task {
                                await viewModel.search()
                            }
                        }) {
                            Image(systemName: "magnifyingglass")
                        }
                        .disabled(viewModel.searchQuery.isEmpty)
                    }
                    
                    if viewModel.isLoading {
                        HStack {
                            Spacer()
                            ProgressView()
                            Spacer()
                        }
                    }
                    
                    if let errorMessage = viewModel.errorMessage {
                        Text(errorMessage)
                            .foregroundColor(.red)
                            .font(.caption)
                    }
                    
                    // Search Results
                    searchResultsView
                }
                
                // Manual Entry Section
                Section("Детали") {
                    TextField("Название *", text: $viewModel.title)
                    
                    TextField(viewModel.selectedType == .book ? "Автор" : "Режиссёр", text: $viewModel.creator)
                    
                    // Status
                    Picker("Статус", selection: $viewModel.status) {
                        ForEach(viewModel.availableStatuses, id: \.self) { status in
                            Text(status.displayTitle).tag(status)
                        }
                    }
                    
                    // Progress
                    if viewModel.selectedType != .movie {
                        HStack {
                            TextField("Текущий", value: $viewModel.progressCurrent, format: .number)
                                .keyboardType(.numberPad)
                            Text("/")
                            TextField("Всего", value: $viewModel.progressTotal, format: .number)
                                .keyboardType(.numberPad)
                        }
                    }
                    
                    // Rating
                    HStack {
                        Text("Оценка")
                        Spacer()
                        StarRatingView(rating: $viewModel.rating)
                    }
                    
                    // Review
                    TextEditor(text: $viewModel.review)
                        .frame(minHeight: 100)
                        .overlay(
                            Alignment.leading.top,
                            alignment: {
                                if viewModel.review.isEmpty {
                                    Text("Личный отзыв...")
                                        .foregroundColor(.gray)
                                        .padding(.horizontal, 5)
                                        .padding(.vertical, 8)
                                }
                            }
                        )
                    
                    // Image
                    Button(action: { showingImagePicker = true }) {
                        HStack {
                            Image(systemName: "photo")
                            Text("Выбрать обложку из галереи")
                        }
                    }
                    
                    if viewModel.imageData != nil, let image = UIImage(data: viewModel.imageData!) {
                        Image(uiImage: image)
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(height: 200)
                            .cornerRadius(8)
                    } else if let url = viewModel.selectedImageURL {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .empty:
                                ProgressView()
                            case .success(let image):
                                image
                                    .resizable()
                                    .aspectRatio(contentMode: .fit)
                                    .frame(height: 200)
                                    .cornerRadius(8)
                            case .failure:
                                Image(systemName: "photo")
                                    .frame(height: 200)
                            @unknown default:
                                EmptyView()
                            }
                        }
                    }
                }
            }
            .navigationTitle("Добавить")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Отмена") {
                        dismiss()
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Сохранить") {
                        viewModel.saveItem()
                        dismiss()
                    }
                    .disabled(viewModel.title.isEmpty)
                    .fontWeight(.semibold)
                }
            }
            .sheet(isPresented: $showingImagePicker) {
                ImagePicker(image: $viewModel.imageData)
            }
        }
        .onAppear {
            viewModel = AddViewModel(modelContext: modelContext)
        }
    }
    
    @ViewBuilder
    private var searchResultsView: some View {
        switch viewModel.searchResults {
        case .empty:
            EmptyView()
            
        case .loading:
            EmptyView()
            
        case .movies(let results):
            ForEach(results, id: \.id) { result in
                SearchResultRow(title: result.displayTitle, 
                               subtitle: result.releaseDate ?? "", 
                               hasImage: result.posterURL != nil)
                    .onTapGesture {
                        viewModel.selectMovie(result)
                    }
            }
            
        case .series(let results):
            ForEach(results, id: \.id) { result in
                SearchResultRow(title: result.displayTitle, 
                               subtitle: result.firstAirDate ?? "", 
                               hasImage: result.posterURL != nil)
                    .onTapGesture {
                        viewModel.selectSeries(result)
                    }
            }
            
        case .books(let results):
            ForEach(results, id: \.id) { result in
                SearchResultRow(title: result.volumeInfo.title, 
                               subtitle: result.volumeInfo.authorName, 
                               hasImage: result.volumeInfo.coverURL != nil)
                    .onTapGesture {
                        viewModel.selectBook(result)
                    }
            }
            
        case .error(let message):
            Text(message)
                .foregroundColor(.red)
                .font(.caption)
        }
    }
}

struct SearchResultRow: View {
    let title: String
    let subtitle: String
    let hasImage: Bool
    
    var body: some View {
        HStack {
            if hasImage {
                Image(systemName: "photo.badge.plus")
                    .foregroundColor(.blue)
            }
            
            VStack(alignment: .leading) {
                Text(title)
                    .font(.headline)
                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundColor(.gray)
        }
    }
}

struct StarRatingView: View {
    @Binding var rating: Double?
    
    var body: some View {
        HStack(spacing: 4) {
            ForEach(1..<6) { index in
                Button(action: {
                    rating = Double(index) * 2
                }) {
                    Image(systemName: index <= (rating ?? 0) / 2 ? "star.fill" : "star")
                        .foregroundColor(.yellow)
                        .font(.system(size: 20))
                }
                .buttonStyle(PlainButtonStyle())
            }
            
            if let rating = rating {
                Text(String(format: "%.0f", rating))
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
    }
}

#Preview {
    AddMediaView()
        .modelContainer(for: MediaItem.self, inMemory: true)
}
