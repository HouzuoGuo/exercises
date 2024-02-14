#include <stdio.h>
#include <unistd.h>

int greet(char *what, int d) { return printf("%s: %d\n", what, d); }

void work() {
  for (int i = 0; i < 1000; i++) {
    greet("hello", i);
    sleep(1);
  }
}

int main(int argc, char **argv) {
  work();
  return 0;
}
