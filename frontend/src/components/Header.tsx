import { Link, useLocation } from 'react-router-dom';
import { Layout, Menu } from 'antd';
import { HomeOutlined, SettingOutlined } from '@ant-design/icons';

const { Header: AntHeader } = Layout;

const Header = () => {
  const location = useLocation();

  const items = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: <Link to="/">论文库</Link>,
    },
    {
      key: '/settings',
      icon: <SettingOutlined />,
      label: <Link to="/settings">LLM 配置</Link>,
    },
  ];

  return (
    <AntHeader style={{ display: 'flex', alignItems: 'center', position: 'sticky', top: 0, zIndex: 100 }}>
      <div style={{ color: 'white', fontSize: 18, fontWeight: 'bold', marginRight: 32 }}>
        📚 Paper Learning
      </div>
      <Menu
        theme="dark"
        mode="horizontal"
        selectedKeys={[location.pathname]}
        items={items}
        style={{ flex: 1 }}
      />
    </AntHeader>
  );
};

export default Header;
