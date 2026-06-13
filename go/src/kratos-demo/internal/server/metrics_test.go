package server

import (
	"errors"
	"testing"

	otelprom "go.opentelemetry.io/otel/exporters/prometheus"
	"go.opentelemetry.io/otel/metric"
)

// errSentinel 是固定的哨兵错误，用于断言 NewMetrics 原样传播依赖初始化错误，
// 而不是吞掉错误、返回部分初始化的实例或 panic。
var errSentinel = errors.New("sentinel error")

// TestNewMetrics_ExporterError 验证：OTel Prometheus exporter 创建失败时，
// NewMetrics 返回该错误且不返回部分初始化的 Metrics。
func TestNewMetrics_ExporterError(t *testing.T) {
	orig := exporterFactory
	exporterFactory = func(opts ...otelprom.Option) (*otelprom.Exporter, error) {
		return nil, errSentinel
	}
	t.Cleanup(func() { exporterFactory = orig })

	m, err := NewMetrics()
	if !errors.Is(err, errSentinel) {
		t.Fatalf("NewMetrics() err = %v, want %v", err, errSentinel)
	}
	if m != nil {
		t.Errorf("NewMetrics() want nil Metrics on exporter error, got %v", m)
	}
}

// TestNewMetrics_CounterError 验证：请求计数 counter 创建失败时，NewMetrics 返回该错误。
func TestNewMetrics_CounterError(t *testing.T) {
	orig := counterFactory
	counterFactory = func(_ metric.Meter, _ string) (metric.Int64Counter, error) {
		return nil, errSentinel
	}
	t.Cleanup(func() { counterFactory = orig })

	m, err := NewMetrics()
	if !errors.Is(err, errSentinel) {
		t.Fatalf("NewMetrics() err = %v, want %v", err, errSentinel)
	}
	if m != nil {
		t.Errorf("NewMetrics() want nil Metrics on counter error, got %v", m)
	}
}

// TestNewMetrics_HistogramError 验证：延迟 histogram 创建失败时，NewMetrics 返回该错误。
func TestNewMetrics_HistogramError(t *testing.T) {
	orig := histogramFactory
	histogramFactory = func(_ metric.Meter, _ string) (metric.Float64Histogram, error) {
		return nil, errSentinel
	}
	t.Cleanup(func() { histogramFactory = orig })

	m, err := NewMetrics()
	if !errors.Is(err, errSentinel) {
		t.Fatalf("NewMetrics() err = %v, want %v", err, errSentinel)
	}
	if m != nil {
		t.Errorf("NewMetrics() want nil Metrics on histogram error, got %v", m)
	}
}
