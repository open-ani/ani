
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    typealias UIViewControllerType = UIViewController
    typealias Context = UIViewControllerRepresentableContext<Self>
    
    func makeUIViewController(context: Context) -> UIViewControllerType {
        AniIosKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
        
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}



