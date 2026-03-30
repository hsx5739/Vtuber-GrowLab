import SwiftUI

struct RootTabView: View {
    @EnvironmentObject private var model: AppModel

    var body: some View {
        TabView {
            HomeScreen()
                .tabItem { Label("首页", systemImage: "house.fill") }
            TasksScreen()
                .tabItem { Label("任务", systemImage: "checklist") }
            EventsScreen()
                .tabItem { Label("事件", systemImage: "sparkles.rectangle.stack") }
            InventoryScreen()
                .tabItem { Label("背包", systemImage: "shippingbox.fill") }
            ProfileScreen()
                .tabItem { Label("我的", systemImage: "person.crop.circle.fill") }
        }
        .tint(MCColor.pink)
        .sheet(item: $model.activeSheet) { sheet in
            switch sheet {
            case .signIn:
                SignInScreen()
            case .gacha:
                GachaScreen()
            case .shop:
                ShopScreen()
            }
        }
    }
}
