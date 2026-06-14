package schema

import (
	"time"

	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

// Greeting 定义问候记录的持久化结构。每次问候请求落库一条记录,
// 含被问候对象 name、生成的问候消息 message、创建时间。
type Greeting struct {
	ent.Schema
}

// Fields 定义问候记录的字段。id 由 ent 默认生成(自增主键)。
func (Greeting) Fields() []ent.Field {
	return []ent.Field{
		field.String("name"),
		field.String("message"),
		field.Time("created_at").
			Default(time.Now),
	}
}
