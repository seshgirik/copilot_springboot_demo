#!/usr/bin/env python3
import sys

def fix_pom():
    try:
        with open('pom.xml', 'r') as f:
            content = f.read()
        
        # Fix the malformed XML tags
        content = content.replace('<n>', '<name>')
        content = content.replace('</n>', '</name>')
        
        with open('pom.xml', 'w') as f:
            f.write(content)
        
        print("✅ Fixed malformed XML tags in pom.xml")
        return True
    except Exception as e:
        print(f"❌ Error fixing pom.xml: {e}")
        return False

if __name__ == "__main__":
    fix_pom()
