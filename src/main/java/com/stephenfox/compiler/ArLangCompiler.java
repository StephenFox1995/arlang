package com.stephenfox.compiler;

import com.stephenfox.ArLangLexer;
import com.stephenfox.ArLangListener;
import com.stephenfox.ArLangParser;
import com.stephenfox.bytecode.BytecodeGenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArLangCompiler {

  private final String text;

  public static void main(String[] args) throws IOException {
    LOGGER.info("{}", Arrays.toString(args));
    if (args.length == 0) {
      LOGGER.info(
          "Please specify the following args: <program>\n"
              + "i.e. java -jar arlang-compiler.jar 11*22");
      System.exit(1);
    }
    new ArLangCompiler(args[0]).compile();
  }

  public ArLangCompiler(String text) {
    this.text = text;
  }

  public void compile() throws IOException {
    // A stream of characters that makes up a program in ArLang.
    CharStream charStream = CharStreams.fromString(text);
    // Run the lexer over the character stream input.
    ArLangLexer lexer = new ArLangLexer(charStream);

    // Generate a token stream from the lexer for the parse to use.
    TokenStream tokens = new CommonTokenStream(lexer);
    ArLangParser parser = new ArLangParser(tokens);

    BytecodeGenerator bytecodeGenerator = new BytecodeGenerator("ArLang");
    ArLangListener arLangListener = new ArLangListenerImpl(bytecodeGenerator);

    ParseTreeWalker.DEFAULT.walk(arLangListener, parser.arlang());

    final Path path = Path.of(FileSystems.getDefault().getPath(".").toString(), "ArLang.class");

    try (FileOutputStream outputStream = new FileOutputStream(path.toString())) {
      outputStream.write(bytecodeGenerator.getBytes());
    }
  }
}