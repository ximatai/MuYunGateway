import {Form, Input, InputNumber, Switch, Card, Row, Col} from 'antd';

const BasicSettings = ({form}) => (
    <Card title="基本设置">
        <div className='card-body'>
            <Row gutter={24}>
                {/* 基本网络配置 */}
                <Col span={12}>
                    <Form.Item label="端口" name="port" rules={[{required: true}]}>
                        <InputNumber min={1} max={65535} placeholder="7071" style={{width: '100%'}}/>
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label="首页路径" name="index" rules={[{required: true}]}>
                        <Input placeholder="/web/"/>
                    </Form.Item>
                </Col>
            </Row>

            {/* HTTPS 配置 */}
            <Form.Item label="开启HTTPS" name={['ssl', 'use']} valuePropName="checked">
                <Switch checkedChildren="开" unCheckedChildren="关"/>
            </Form.Item>
            <Form.Item
                label="证书路径"
                name={['ssl', 'certPath']}
                rules={[
                    ({getFieldValue}) => ({
                        validator(_, value) {
                            if (!getFieldValue(['ssl', 'use']) || value) {
                                return Promise.resolve();
                            }
                            return Promise.reject(new Error('请输入证书路径'));
                        },
                    }),
                ]}
            >
                <Input placeholder="/path/to/cert"/>
            </Form.Item>
            <Form.Item
                label="密钥路径"
                name={['ssl', 'keyPath']}
                rules={[
                    ({getFieldValue}) => ({
                        validator(_, value) {
                            if (!getFieldValue(['ssl', 'use']) || value) {
                                return Promise.resolve();
                            }
                            return Promise.reject(new Error('请输入密钥路径'));
                        },
                    }),
                ]}
            >
                <Input placeholder="/path/to/key"/>
            </Form.Item>

            {/* 登录设置 */}
            <Row gutter={24}>
                <Col span={12}>
                    <Form.Item label="登录路径" name={['login', 'path']} rules={[{required: true}]}>
                        <Input placeholder="/api/sso/login"/>
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label="登录页面" name={['login', 'page']} rules={[{required: true}]}>
                        <Input placeholder="/web/login"/>
                    </Form.Item>
                </Col>
            </Row>
            <Form.Item label="登录接口" name={['login', 'api']} rules={[{required: true}]}>
                <Input placeholder="http://127.0.0.1:8080/api/sso/login"/>
            </Form.Item>

            {/* 认证设置 */}
            <Form.Item label="开启JWT" name={['jwt', 'use']} valuePropName="checked">
                <Switch checkedChildren="开" unCheckedChildren="关"/>
            </Form.Item>
            <Form.Item
                label="验证过期时间"
                name={['jwt', 'checkExpiration']}
                valuePropName="checked"
                dependencies={['jwt', 'use']}
            >
                <Switch checkedChildren="开" unCheckedChildren="关"/>
            </Form.Item>
            <Form.Item
                label="JWT过期时间（分钟）"
                name={['jwt', 'expiresMin']}
                dependencies={['jwt', 'use']}
                rules={[
                    ({getFieldValue}) => ({
                        validator(_, value) {
                            if (!getFieldValue(['jwt', 'use']) || value) {
                                return Promise.resolve();
                            }
                            return Promise.reject(new Error('请输入JWT过期时间'));
                        },
                    }),
                ]}
            >
                <InputNumber placeholder="43200" style={{width: '100%'}}/>
            </Form.Item>
            <Form.Item label="开启Session" name={['session', 'use']} valuePropName="checked">
                <Switch checkedChildren="开" unCheckedChildren="关"/>
            </Form.Item>
            <Form.Item
                label="Session过期时间（小时）"
                name={['session', 'timeoutHour']}
                dependencies={['session', 'use']}
                rules={[
                    ({getFieldValue}) => ({
                        validator(_, value) {
                            if (!getFieldValue(['session', 'use']) || value) {
                                return Promise.resolve();
                            }
                            return Promise.reject(new Error('请输入Session过期时间'));
                        },
                    }),
                ]}
            >
                <InputNumber min={1} placeholder="24" style={{width: '100%'}}/>
            </Form.Item>
        </div>
    </Card>
);

export default BasicSettings;
