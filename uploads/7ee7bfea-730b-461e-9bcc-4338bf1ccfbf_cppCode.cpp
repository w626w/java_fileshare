#include <bits/stdc++.h>
using namespace std;
typedef long long LL;
const LL N = 2e5 + 10;
// 节点和权重
vector<pair<int, int> > adj[N];
// dep深度,fa父节点,dis到根节点的距离
int dep[N], fa[N][25], dis[N];

void bfs(int p, int fath) {
  dep[p] = dep[fath] + 1;
  fa[p][0] = fath;
  for (int i = 1; (1 << i) <= dep[p]; i++) fa[p][i] = fa[fa[p][i - 1]][i - 1];

  for (auto [s, val] : adj[p]) {
    if (s == fath) continue;
    dis[s] = dis[p] + val;
    bfs(s, p);
  }
}

int lca(int a, int b) {
  if (dep[a] < dep[b]) swap(a, b);
  for (int i = 20; i >= 0; i--)
    if (dep[a] - (1 << i) >= dep[b]) a = fa[a][i];
  if (a == b) return a;
  // 拉平a,b的深度

  for (int i = 20; i >= 0; i--)
    if (fa[a][i] != fa[b][i]) a = fa[a][i], b = fa[b][i];
  // 只有跳上去两个点不在同一位置才跳，如果是同一位置测停止跳
  return fa[a][0];  // 返回最后相等的一个父节点
}

void solve() {
  int n, m, s;
  cin >> n >> m >> s;
  for (int i = 1; i <= n; i++) {
    int u, v, w;
    cin >> u >> v >> w;
    adj[u].push_back({v, w});
    adj[v].push_back({u, w});
  }
  bfs(s, 0);
  while (m--) {
    int a, b;
    cin >> a >> b;
    cout << lca(a, b) << "\n";
  }
}

int main() {
  int _ = 1;
  while (_--) {
    solve();
  }

  return 0;
}
