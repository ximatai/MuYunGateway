import { DragOutlined, MinusCircleOutlined, PlusOutlined } from '@ant-design/icons'
import { Button, Card, Col, Collapse, Form, Input, Row, Space, Switch } from 'antd'
import { DragDropContext, Draggable, Droppable } from 'react-beautiful-dnd'

const { Panel } = Collapse

function FrontendSettings({ form }) {
  const onDragEnd = (result) => {
    if (!result.destination)
      return

    const items = [...form.getFieldValue('frontends')]
    const [reorderedItem] = items.splice(result.source.index, 1)
    items.splice(result.destination.index, 0, reorderedItem)

    form.setFieldsValue({ frontends: items })
  }

  return (
    <Card
      title="前端设置"
      bodyStyle={{
        maxHeight: 'calc(100vh - 200px)',
        overflow: 'auto',
      }}
      extra={(
        <Button
          type="dashed"
          onClick={() => {
            const frontends = form.getFieldValue('frontends') || []
            form.setFieldsValue({
              frontends: [
                ...frontends,
                {
                  path: '',
                  dir: '',
                  notFoundReroute: '/',
                  secured: false,
                  regex: false,
                  noCache: [],
                  allowlist: [],
                },
              ],
            })
          }}
        >
          <PlusOutlined />
          {' '}
          添加前端配置
        </Button>
      )}
    >
      <DragDropContext onDragEnd={onDragEnd}>
        <Droppable droppableId="frontends">
          {provided => (
            <Collapse
              accordion
              ref={provided.innerRef}
              {...provided.droppableProps}
              bordered={false}
            >
              {(form.getFieldValue('frontends') || []).length === 0 && (
                <p style={{ textAlign: 'center', color: '#aaa' }}>No configurations added</p>
              )}

              {(form.getFieldValue('frontends') || []).map((frontend, index) => (
                <Draggable key={index} draggableId={`frontend-${index}`} index={index}>
                  {(provided, snapshot) => (
                    <Panel
                      header={`前缀: ${frontend.path || '未定义'}`}
                      key={index}
                      ref={provided.innerRef}
                      {...provided.draggableProps}
                      extra={(
                        <Space>
                          <DragOutlined
                            {...provided.dragHandleProps}
                            style={{
                              cursor: snapshot.isDragging ? 'grabbing' : 'grab',
                            }}
                          />
                          <MinusCircleOutlined
                            onClick={() => {
                              const frontends = form.getFieldValue('frontends') || []
                              frontends.splice(index, 1)
                              form.setFieldsValue({ frontends })
                            }}
                          />
                        </Space>
                      )}
                    >
                      <Row gutter={24}>
                        <Col span={12}>
                          <Form.Item
                            name={['frontends', index, 'path']}
                            label="前缀路径"
                            rules={[{ required: true, message: '请输入前缀路径' }]}
                          >
                            <Input placeholder="/web" />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item
                            name={['frontends', index, 'dir']}
                            label="资源目录"
                            rules={[{ required: true, message: '请输入资源目录' }]}
                          >
                            <Input placeholder="/path/to/resources" />
                          </Form.Item>
                        </Col>
                      </Row>
                      <Form.Item
                        name={['frontends', index, 'notFoundReroute']}
                        label="404 重路由"
                      >
                        <Input placeholder="/" />
                      </Form.Item>
                      <Form.Item
                        name={['frontends', index, 'secured']}
                        label="是否安全"
                        valuePropName="checked"
                      >
                        <Switch checkedChildren="是" unCheckedChildren="否" />
                      </Form.Item>
                      <Form.Item
                        name={['frontends', index, 'regex']}
                        label="正则匹配"
                        valuePropName="checked"
                      >
                        <Switch checkedChildren="开" unCheckedChildren="关" />
                      </Form.Item>

                      {/* 动态数组：不缓存 */}
                      <Form.List name={['frontends', index, 'noCache']}>
                        {(fields, { add, remove }) => (
                          <>
                            <label>不缓存路径</label>
                            {fields.map(({ key, name, ...restField }) => (
                              <Row key={key} gutter={8} align="middle">
                                <Col span={20}>
                                  <Form.Item
                                    {...restField}
                                    name={name}
                                    rules={[{ required: true, message: '请输入不缓存路径' }]}
                                  >
                                    <Input placeholder="/path" />
                                  </Form.Item>
                                </Col>
                                <Col span={4}>
                                  <MinusCircleOutlined onClick={() => remove(name)} />
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
                      <Form.List name={['frontends', index, 'allowlist']}>
                        {(fields, { add, remove }) => (
                          <>
                            <label>白名单</label>
                            {fields.map(({ key, name, ...restField }) => (
                              <Row key={key} gutter={8} align="middle">
                                <Col span={20}>
                                  <Form.Item
                                    {...restField}
                                    name={name}
                                    rules={[{ required: true, message: '请输入白名单路径' }]}
                                  >
                                    <Input placeholder="/path" />
                                  </Form.Item>
                                </Col>
                                <Col span={4}>
                                  <MinusCircleOutlined onClick={() => remove(name)} />
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
                    </Panel>
                  )}
                </Draggable>
              ))}
              {provided.placeholder}
            </Collapse>
          )}
        </Droppable>
      </DragDropContext>
    </Card>
  )
}

export default FrontendSettings
