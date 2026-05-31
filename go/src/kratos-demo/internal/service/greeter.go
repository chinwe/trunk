package service

import (
	"context"

	pb "kratos-demo/api/helloworld/v1"
	"kratos-demo/internal/biz"

	"github.com/go-kratos/kratos/v2/log"
)

// GreeterService 问候服务实现
type GreeterService struct {
	pb.UnimplementedGreeterServiceServer

	uc  *biz.GreeterUsecase
	log *log.Helper
}

// NewGreeterService 创建问候服务
func NewGreeterService(uc *biz.GreeterUsecase, logger log.Logger) *GreeterService {
	return &GreeterService{
		uc:  uc,
		log: log.NewHelper(logger),
	}
}

// SayHello 处理问候请求
func (s *GreeterService) SayHello(ctx context.Context, req *pb.HelloRequest) (*pb.HelloReply, error) {
	s.log.WithContext(ctx).Infof("SayHello request: name=%s", req.Name)
	message, err := s.uc.SayHello(ctx, req.Name)
	if err != nil {
		return nil, err
	}
	return &pb.HelloReply{Message: message}, nil
}
