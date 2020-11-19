#include <stdio.h>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <stdlib.h>
#include <cstring>

const char* socket_path = "ipc.socket";

int main(int argc, char *argv[])
{
  // 命令行参数指定路径
  if (argc > 1)
  {
    socket_path = argv[1];
  }

  // 创建Unix Domain Socket
  int fd = socket(AF_UNIX, SOCK_STREAM, 0);
  if (-1 == fd)
  {
    perror("socket error");
    exit(-1);
  }

  // bind
  struct sockaddr_un addr;
  memset(&addr, 0, sizeof(addr));
  addr.sun_family = AF_UNIX;
  strncpy(addr.sun_path, socket_path, sizeof(addr.sun_path) - 1);
  unlink(socket_path);
  if (-1 == bind(fd, (struct sockaddr *)&addr, sizeof(addr)))
  {
    perror("bind error");
    exit(-1);
  }

  // listen
  if (-1 == listen(fd, 1000))
  {
    perror("listen error");
    exit(-1);
  }

  char buf[1024] = { 0 };
  while (true)
  {
    int cl = accept(fd, NULL, NULL);
    if (-1 == cl)
    {
      perror("accept error");
      continue;
    }
    
    int rc = 0;
    while (read(cl, buf, sizeof(buf)) > 0)
    {
      printf("read %u bytes: %.*s\n", rc, rc, buf);
    }
    if (-1 == rc)
    {
      perror("read");
      exit(-1);
    }
    else if (0 == rc)
    {
      printf("EOF\n");
      close(cl);
    }
  }

  return 0;
}