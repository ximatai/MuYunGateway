import { CheckCircleOutlined, ClearOutlined, UploadOutlined } from '@ant-design/icons'
import { Button, Col, Divider, Form, message, Row, Space } from 'antd'
import { useEffect } from 'react'
import BackendSettings from './BackendSettings'
import BasicSettings from './BasicSettings'
import FrontendSettings from './FrontendSettings'

function Setting() {
  const [form] = Form.useForm()

  useEffect(() => {
    // 页面加载时调用后端接口
    fetch('./api/config')
      .then((response) => {
        if (!response.ok) {
          throw new Error('Failed to fetch configuration')
        }
        return response.json()
      })
      .then((data) => {
        form.setFieldsValue(data)
      })
      .catch((error) => {
        console.error('Error loading configuration:', error)
        message.error('Failed to load configuration')
      })
  }, [form])

  const initialValues = {
    port: 7070,
    index: '/web/',
    ssl: { use: false, certPath: null, keyPath: null },
    login: { path: '/api/sso/login', page: '/web/login', api: 'http://127.0.0.1:8080/api/sso/login' },
    jwt: { use: true, checkExpiration: false, expiresMin: null },
    session: { use: true, timeoutHour: 24 },
    redirects: [],
    frontends: [],
    upstreams: [],
  }

  const onFinish = (values) => {
    console.log('Form values:', values)
    fetch('./api/config', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(values),
    })
      .then(res => res.json())
      .then(() => {
        message.success('Settings applied successfully')
      })
      .catch(() => {
        message.error('Failed to apply settings')
      })
  }

  const handleImport = (file) => {
    const reader = new FileReader()
    reader.onload = () => {
      const data = JSON.parse(reader.result)
      form.setFieldsValue(data)
      message.success('Configuration imported successfully')
    }
    reader.readAsText(file)
  }

  const handleExport = () => {
    const values = form.getFieldsValue()
    const dataStr = JSON.stringify(values, null, 2)
    const blob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(blob)

    const a = document.createElement('a')
    a.href = url
    a.download = 'gateway-config.json'
    a.click()
    URL.revokeObjectURL(url)
    message.success('Configuration exported successfully')
  }

  return (
    <Form
      form={form}
      onFinish={onFinish}
      layout="vertical"
      initialValues={initialValues}
      scrollToFirstError
    >
      <Row gutter={24}>
        <Col xs={24} md={8}>
          <BasicSettings form={form} />
        </Col>
        <Col xs={24} md={8}>
          <FrontendSettings form={form} />
        </Col>
        <Col xs={24} md={8}>
          <BackendSettings form={form} />
        </Col>
      </Row>

      <Divider />
      <Space>
        <Button htmlType="submit" type="primary" icon={<CheckCircleOutlined />}>
          保存设置
        </Button>
        <Button
          type="default"
          icon={<ClearOutlined />}
          onClick={() => form.resetFields()}
        >
          初始化
        </Button>
        <Button
          type="default"
          icon={<UploadOutlined />}
          onClick={() => document.getElementById('fileInput').click()}
        >
          导入
        </Button>
        <Button type="default" onClick={handleExport}>
          导出
        </Button>
        <input
          type="file"
          id="fileInput"
          style={{ display: 'none' }}
          accept=".json"
          onChange={e => handleImport(e.target.files[0])}
        />
      </Space>
    </Form>
  )
}

export default Setting
