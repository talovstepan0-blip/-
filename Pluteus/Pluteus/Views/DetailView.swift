//
//  DetailView.swift
//  Pluteus
//
//  Created by Developer on 2024.
//

import SwiftUI
import SwiftData

struct DetailView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    let item: MediaItem
    @State private var viewModel: DetailViewModel!
    @State private var showingImagePicker = false
    @State private var showingDeleteAlert = false
    
    var body: some View {
        Form {
            // Cover Image
            Section {
                if let imageData = viewModel.imageData, let image = UIImage(data: imageData) {
                    Image(uiImage: image)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(maxWidth: 300)
                        .cornerRadius(8)
                        .onTapGesture {
                            if viewModel.isEditing {
                                showingImagePicker = true
                            }
                        }
                    
                    if viewModel.isEditing {
                        Button("Изменить обложку") {
                            showingImagePicker = true
                        }
                    }
                } else {
                    ZStack {
                        Rectangle()
                            .fill(Color(.systemGray4))
                            .frame(height: 300)
                            .cornerRadius(8)
                        
                        Image(systemName: getIconName())
                            .font(.system(size: 60))
                            .foregroundColor(.white)
                    }
                    
                    if viewModel.isEditing {
                        Button("Добавить обложку") {
                            showingImagePicker = true
                        }
                    }
                }
            }
            
            // Basic Info
            Section("Информация") {
                if viewModel.isEditing {
                    TextField("Название", text: $viewModel.title)
                    TextField(viewModel.mediaType == .book ? "Автор" : "Режиссёр", text: $viewModel.creator)
                } else {
                    LabeledContent("Название", value: viewModel.title)
                    if !viewModel.creator.isEmpty {
                        LabeledContent(viewModel.mediaType == .book ? "Автор" : "Режиссёр", value: viewModel.creator)
                    }
                }
                
                // Type
                LabeledContent("Тип", value: viewModel.mediaType.displayName)
                
                // Status
                if viewModel.isEditing {
                    Picker("Статус", selection: $viewModel.status) {
                        ForEach(viewModel.availableStatuses, id: \.self) { status in
                            Text(status.displayTitle).tag(status)
                        }
                    }
                } else {
                    LabeledContent("Статус", value: viewModel.status.displayTitle)
                }
            }
            
            // Progress
            if viewModel.mediaType != .movie {
                Section("Прогресс") {
                    if viewModel.isEditing {
                        HStack {
                            TextField("Текущий", value: $viewModel.progressCurrent, format: .number)
                                .keyboardType(.numberPad)
                            Text("/")
                            TextField("Всего", value: $viewModel.progressTotal, format: .number)
                                .keyboardType(.numberPad)
                        }
                    } else {
                        if viewModel.progressTotal > 0 {
                            VStack(alignment: .leading, spacing: 8) {
                                ProgressView(value: viewModel.progressPercentage)
                                    .progressViewStyle(LinearProgressViewStyle(tint: .blue))
                                
                                HStack {
                                    Text("\(viewModel.progressCurrent) из \(viewModel.progressTotal)")
                                    Spacer()
                                    Text(String(format: "%.0f%%", viewModel.progressPercentage * 100))
                                        .foregroundColor(.secondary)
                                }
                                .font(.caption)
                            }
                        } else {
                            Text("Количество страниц/серий не указано")
                                .foregroundColor(.secondary)
                        }
                    }
                }
            }
            
            // Rating
            Section("Оценка") {
                if viewModel.isEditing {
                    HStack {
                        Text("Оценка")
                        Spacer()
                        StarRatingView(rating: $viewModel.rating)
                    }
                } else {
                    if let rating = viewModel.rating, rating > 0 {
                        HStack {
                            ForEach(0..<5) { index in
                                Image(systemName: index < Int(rating / 2) ? "star.fill" : "star")
                                    .foregroundColor(.yellow)
                            }
                            Spacer()
                            Text(String(format: "%.1f / 10", rating))
                                .foregroundColor(.secondary)
                        }
                    } else {
                        Text("Оценка не поставлена")
                            .foregroundColor(.secondary)
                    }
                }
            }
            
            // Review
            Section("Отзыв") {
                if viewModel.isEditing {
                    TextEditor(text: $viewModel.review)
                        .frame(minHeight: 100)
                } else {
                    if !viewModel.review.isEmpty {
                        Text(viewModel.review)
                    } else {
                        Text("Отзыв отсутствует")
                            .foregroundColor(.secondary)
                    }
                }
            }
            
            // Date Added
            Section("Дата добавления") {
                Text(item.dateAdded, style: .date)
            }
            
            // Actions
            if !viewModel.isEditing {
                Section {
                    Button("Редактировать") {
                        viewModel.isEditing = true
                    }
                }
                
                Section {
                    Button(role: .destructive) {
                        showingDeleteAlert = true
                    } label: {
                        HStack {
                            Spacer()
                            Text("Удалить")
                            Spacer()
                        }
                    }
                }
            } else {
                Section {
                    HStack {
                        Button("Отмена") {
                            viewModel.cancelEditing()
                        }
                        .frame(maxWidth: .infinity)
                        
                        Button("Сохранить") {
                            viewModel.saveChanges()
                        }
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity)
                    }
                }
            }
        }
        .navigationTitle(viewModel.title)
        .navigationBarTitleDisplayMode(.inline)
        .alert("Удалить запись?", isPresented: $showingDeleteAlert) {
            Button("Отмена", role: .cancel) {}
            Button("Удалить", role: .destructive) {
                viewModel.deleteItem()
                dismiss()
            }
        } message: {
            Text("Вы уверены, что хотите удалить \"\(viewModel.title)\"? Это действие нельзя отменить.")
        }
        .sheet(isPresented: $showingImagePicker) {
            ImagePicker(image: $viewModel.imageData)
        }
        .onAppear {
            viewModel = DetailViewModel(item: item, modelContext: modelContext)
        }
    }
    
    private func getIconName() -> String {
        switch viewModel?.mediaType ?? .movie {
        case .movie: return "film"
        case .series: return "tv"
        case .book: return "book"
        }
    }
}

#Preview {
    NavigationView {
        DetailView(item: MediaItem(
            type: .movie,
            title: "Начало",
            creator: "Кристофер Нолан",
            status: .completed,
            rating: 9.0,
            review: "Отличный фильм!"
        ))
    }
    .modelContainer(for: MediaItem.self, inMemory: true)
}
