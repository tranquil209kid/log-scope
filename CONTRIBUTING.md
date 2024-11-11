# Contributing to LogScope

Thank you for your interest in contributing to LogScope! This document provides guidelines for contributing to the project.

## License Agreement

By contributing to LogScope, you agree that your contributions will be licensed under the European Union Public License (EUPL) v1.2. All modifications and additions must maintain this license to ensure consistency across the project.

## Code Style Guidelines

### General Principles
- Write clean, readable, and maintainable code
- Optimize code where possible without sacrificing readability
- Keep the codebase organized and consistent
- Follow existing project structure and patterns

### Naming Conventions
- Use camelCase for variable and method names
  ```java
  // Good
  private int userCount;
  public void calculateTotal();
  
  // Bad
  private int user_count;
  public void Calculate_Total();
  ```
- Use PascalCase for class names
  ```java
  // Good
  public class LogViewer
  
  // Bad
  public class logViewer
  ```
- Choose descriptive and meaningful names that reflect purpose

### Documentation
- All changes must be properly documented
- Add comments explaining:
    - What the change does
    - Why the change was made
    - Any important implementation details
  ```java
  // Added scrolling functionality to log viewer
  // This improves UX by allowing users to navigate through long logs
  // Implementation uses native Minecraft scroll handling
  public void handleScroll() {
      // ...
  }
  ```

### Code Organization
- Keep methods focused and single-purpose
- Group related functionality together
- Use appropriate access modifiers
- Maintain a clear and logical file structure

## Pull Request Process

1. Fork the repository
2. Create a new branch for your feature/fix
3. Make your changes following the guidelines above
4. Test your changes thoroughly
5. Update documentation if needed
6. Submit a pull request with a clear description of:
    - What changes were made
    - Why the changes were needed
    - Any relevant issue numbers

## Pull Request Checklist

Before submitting your pull request, ensure:

- [ ] Code follows style guidelines
- [ ] Comments explain changes
- [ ] All tests pass
- [ ] Documentation is updated
- [ ] Changes are licensed under EUPL v1.2
- [ ] Branch is up to date with main

## Need Help?

If you have questions or need clarification about these guidelines, feel free to:
- Open an issue for discussion
- Reach out to the maintainers
- Check existing code for examples

Thank you for helping make LogScope better!

---

*Note: These guidelines may be updated over time. Check back occasionally for any changes.*