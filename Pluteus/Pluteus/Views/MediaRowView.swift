//
//  MediaRowView.swift
//  Pluteus
//
//  Created by Developer on 2024.
//

import SwiftUI

struct MediaRowView: View {
    let item: MediaItem
    
    var body: some View {
        HStack(spacing: 12) {
            // Thumbnail
            if let imageData = item.imageData, let uiImage = UIImage(data: imageData) {
                Image(uiImage: uiImage)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: 60, height: 85)
                    .clipped()
                    .cornerRadius(8)
            } else {
                ZStack {
                    Rectangle()
                        .fill(Color(.systemGray4))
                        .frame(width: 60, height: 85)
                        .cornerRadius(8)
                    
                    Image(systemName: getIconName())
                        .font(.system(size: 24))
                        .foregroundColor(.white)
                }
            }
            
            VStack(alignment: .leading, spacing: 4) {
                // Title
                Text(item.title)
                    .font(.headline)
                    .lineLimit(2)
                
                // Creator
                if !item.creator.isEmpty {
                    Text(item.creator)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
                
                // Status and Type
                HStack {
                    Text(item.type.displayName)
                        .font(.caption)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color(.systemGray5))
                        .cornerRadius(4)
                    
                    Text(item.status.displayTitle)
                        .font(.caption)
                        .foregroundColor(getStatusColor())
                }
                
                // Progress
                if item.type != .movie && item.progressTotal > 0 {
                    VStack(alignment: .leading, spacing: 2) {
                        ProgressView(value: item.progressPercentage)
                            .progressViewStyle(LinearProgressViewStyle(tint: .blue))
                        
                        Text("\(item.progressCurrent) из \(item.progressTotal)")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }
                
                // Rating
                if let rating = item.rating, rating > 0 {
                    HStack(spacing: 2) {
                        ForEach(0..<5) { index in
                            Image(systemName: index < Int(rating / 2) ? "star.fill" : "star")
                                .font(.caption2)
                                .foregroundColor(.yellow)
                        }
                        Text(String(format: "%.1f", rating))
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }
            }
            
            Spacer()
        }
        .padding(.vertical, 4)
    }
    
    private func getIconName() -> String {
        switch item.type {
        case .movie: return "film"
        case .series: return "tv"
        case .book: return "book"
        }
    }
    
    private func getStatusColor() -> Color {
        switch item.status {
        case .planned:
            return .secondary
        case .watching, .reading:
            return .blue
        case .completed, .read:
            return .green
        }
    }
}

#Preview {
    List {
        MediaRowView(item: MediaItem(
            type: .movie,
            title: "Начало",
            creator: "Кристофер Нолан",
            status: .completed,
            rating: 9.0
        ))
        
        MediaRowView(item: MediaItem(
            type: .series,
            title: "Во все тяжкие",
            creator: "Винс Гиллиган",
            status: .watching,
            progressCurrent: 30,
            progressTotal: 62
        ))
        
        MediaRowView(item: MediaItem(
            type: .book,
            title: "1984",
            creator: "Джордж Оруэлл",
            status: .reading,
            progressCurrent: 150,
            progressTotal: 328
        ))
    }
}
