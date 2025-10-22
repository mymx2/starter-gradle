package io.github.mymx2.java;

/// example-java application
///
/// @author dy
public class JavaApp {

  /// say hello
  ///
  /// @return hello java
  public String sayHello() {
    return "hello java";
  }

  /// main function
  @SuppressWarnings("SystemOut")
  void main() {
    IO.println(new JavaApp().sayHello());
  }
}
