import { ClusterOutlined } from '@ant-design/icons'
import { Layout, Menu, theme } from 'antd'
import { Navigate, Route, BrowserRouter as Router, Routes, useLocation, useNavigate } from 'react-router-dom'
import Dashboard from './pages/Dashboard' // 引入 Dashboard 页面
import Setting from './pages/Setting' // 引入 Setting 页面

const { Header, Content, Footer } = Layout

function App() {
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken()
  const navigate = useNavigate()
  const location = useLocation() // 替代 window.location

  return (
    <Layout style={{ height: '100%' }}>
      <Header>
        <ClusterOutlined className="logo" />
        <Menu
          theme="dark"
          mode="horizontal"
          selectedKeys={[location.pathname]}
          onClick={({ key }) => navigate(key)}
        >
          <Menu.Item key="/dashboard">
            仪表盘
          </Menu.Item>
          <Menu.Item key="/setting">
            网关设置
          </Menu.Item>
        </Menu>
      </Header>
      <Content className="main-content">
        <div
          className="content-container"
          style={{
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
          }}
        >
          <Routes>
            <Route path="/" element={<Navigate to="/setting" replace />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/setting" element={<Setting />} />
          </Routes>
        </div>
      </Content>
      <Footer style={{ textAlign: 'center' }}>
        Ant Design ©
        {new Date().getFullYear()}
        {' '}
        Created by Ant UED
      </Footer>
    </Layout>
  )
}

function Root() {
  return (
    <Router basename="/gw">
      <App />
    </Router>
  )
}

export default Root
