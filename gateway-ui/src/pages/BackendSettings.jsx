import { DragOutlined, MinusCircleOutlined, PlusOutlined } from '@ant-design/icons'
import { Button, Card, Col, Collapse, Form, Input, InputNumber, Row, Space, Switch } from 'antd'
import { DragDropContext, Draggable, Droppable } from 'react-beautiful-dnd'

const { Panel } = Collapse

function BackendSettings({ form }) {
  const onDragEnd = (result) => {
    if (!result.destination)
      return

    const items = form.getFieldValue('upstreams')
    const [reorderedItem] = items.splice(result.source.index, 1)
    items.splice(result.destination.index, 0, reorderedItem)

    form.setFieldsValue({ upstreams: items })
  }

  return (
    <Card
      title="后端设置"
      extra={(
        <Button
          type="dashed"
          onClick={() => {
            const upstreams = form.getFieldValue('upstreams') || []
            form.setFieldsValue({
              upstreams: [
                ...upstreams,
                {
                  path: '',
                  secured: false,
                  regex: false,
                  noCache: [],
                  allowlist: [],
                  backends: [{ url: '', weight: 1 }],
                },
              ],
            })
          }}
        >
          <PlusOutlined />
          {' '}
          添加后端配置
        </Button>
      )}
    >
      <div className="card-body">
        <DragDropContext onDragEnd={onDragEnd}>
          <Droppable droppableId="upstreams">
            {provided => (
              <Collapse
                accordion
                ref={provided.innerRef}
                {...provided.droppableProps}
                bordered={false}
              >
                {form.getFieldValue('upstreams')?.map((upstream, index) => (
                  <Draggable key={index} draggableId={`upstream-${index}`} index={index}>
                    {provided => (
                      <Panel
                        header={`路径: ${upstream.path || '未定义'}`}
                        key={index}
                        ref={provided.innerRef}
                        {...provided.draggableProps}
                        extra={(
                          <Space>
                            <DragOutlined {...provided.dragHandleProps} />
                            <MinusCircleOutlined
                              onClick={() => {
                                const upstreams = form.getFieldValue('upstreams') || []
                                upstreams.splice(index, 1)
                                form.setFieldsValue({ upstreams })
                              }}
                            />
                          </Space>
                        )}
                      >
                        <Row gutter={24}>
                          <Col span={12}>
                            <Form.Item
                              name={['upstreams', index, 'path']}
                              label="代理路径"
                              rules={[{ required: true, message: '请输入代理路径' }]}
                            >
                              <Input placeholder="/api" />
                            </Form.Item>
                          </Col>
                          <Col span={6}>
                            <Form.Item
                              name={['upstreams', index, 'secured']}
                              label="是否安全"
                              valuePropName="checked"
                            >
                              <Switch checkedChildren="是" unCheckedChildren="否" />
                            </Form.Item>
                          </Col>
                          <Col span={6}>
                            <Form.Item
                              name={['upstreams', index, 'regex']}
                              label="正则匹配"
                              valuePropName="checked"
                            >
                              <Switch checkedChildren="开" unCheckedChildren="关" />
                            </Form.Item>
                          </Col>
                        </Row>

                        {/* 动态数组：不缓存 */}
                        <Form.List name={['upstreams', index, 'noCache']}>
                          {(fields, { add, remove }) => (
                            <>
                              <label>不缓存路径</label>
                              {fields.map(({ key, name, ...restField }) => (
                                <Row key={key} gutter={8} align="middle">
                                  <Col span={20}>
                                    <Form.Item
                                      {...restField}
                                      name={name}
                                      rules={[{
                                        required: true,
                                        message: '请输入不缓存路径',
                                      }]}
                                    >
                                      <Input placeholder="/path" />
                                    </Form.Item>
                                  </Col>
                                  <Col span={4}>
                                    <MinusCircleOutlined
                                      onClick={() => remove(name)}
                                    />
                                  </Col>
                                </Row>
                              ))}
                              <Button
                                type="dashed"
                                onClick={() => add()}
                                block
                                icon={<PlusOutlined />}
                              >
                                添加不缓存路径
                              </Button>
                            </>
                          )}
                        </Form.List>

                        {/* 动态数组：白名单 */}
                        <Form.List name={['upstreams', index, 'allowlist']}>
                          {(fields, { add, remove }) => (
                            <>
                              <label>白名单</label>
                              {fields.map(({ key, name, ...restField }) => (
                                <Row key={key} gutter={8} align="middle">
                                  <Col span={20}>
                                    <Form.Item
                                      {...restField}
                                      name={name}
                                      rules={[{
                                        required: true,
                                        message: '请输入白名单路径',
                                      }]}
                                    >
                                      <Input placeholder="/path" />
                                    </Form.Item>
                                  </Col>
                                  <Col span={4}>
                                    <MinusCircleOutlined
                                      onClick={() => remove(name)}
                                    />
                                  </Col>
                                </Row>
                              ))}
                              <Button
                                type="dashed"
                                onClick={() => add()}
                                block
                                icon={<PlusOutlined />}
                              >
                                添加白名单路径
                              </Button>
                            </>
                          )}
                        </Form.List>

                        {/* 嵌套动态数组：后端服务 */}
                        <Form.List name={['upstreams', index, 'backends']}>
                          {(fields, { add, remove }) => (
                            <>
                              <label>后端服务</label>
                              {fields.map(({ key, name, ...restField }) => (
                                <Row key={key} gutter={8} align="middle">
                                  <Col span={14}>
                                    <Form.Item
                                      {...restField}
                                      name={[name, 'url']}
                                      rules={[{
                                        required: true,
                                        message: '请输入后端服务地址',
                                      }]}
                                    >
                                      <Input
                                        placeholder="http://127.0.0.1:8080/api"
                                      />
                                    </Form.Item>
                                  </Col>
                                  <Col span={6}>
                                    <Form.Item
                                      {...restField}
                                      name={[name, 'weight']}
                                      rules={[{
                                        required: true,
                                        message: '请输入权重',
                                      }]}
                                    >
                                      <InputNumber
                                        min={1}
                                        max={10}
                                        placeholder="1"
                                      />
                                    </Form.Item>
                                  </Col>
                                  <Col span={4}>
                                    <MinusCircleOutlined
                                      onClick={() => remove(name)}
                                    />
                                  </Col>
                                </Row>
                              ))}
                              <Button
                                type="dashed"
                                onClick={() => add({ url: '', weight: 1 })}
                                block
                                icon={<PlusOutlined />}
                              >
                                添加后端服务
                              </Button>
                            </>
                          )}
                        </Form.List>
                      </Panel>
                    )}
                  </Draggable>
                ))}
                {provided.placeholder}
              </Collapse>
            )}
          </Droppable>
        </DragDropContext>
      </div>
    </Card>
  )
}

export default BackendSettings
