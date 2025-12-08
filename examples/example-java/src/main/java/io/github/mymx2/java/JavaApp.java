package io.github.mymx2.java;

/// example-java application
///
/// @author dy
public class JavaApp {

  /// say hello
  ///
  /// @return Hello, Java!
  public String sayHello() {
    return "Hello, Java!";
  }

  /// main function
  @SuppressWarnings("SystemOut")
  void main() {
    IO.println(new JavaApp().sayHello());
  }
}
