/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.util.xml;

import java.io.BufferedReader;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Detects whether an XML stream is using DTD- or XSD-based validation.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class XmlValidationModeDetector {

	/**
	 * Indicates that the validation should be disabled.
	 */
	public static final int VALIDATION_NONE = 0;

	/**
	 * Indicates that the validation mode should be auto-guessed, since we cannot find
	 * a clear indication (probably choked on some special characters, or the like).
	 */
	public static final int VALIDATION_AUTO = 1;

	/**
	 * Indicates that DTD validation should be used (we found a "DOCTYPE" declaration).
	 */
	public static final int VALIDATION_DTD = 2;

	/**
	 * Indicates that XSD validation should be used (found no "DOCTYPE" declaration).
	 */
	public static final int VALIDATION_XSD = 3;


	/**
	 * The token in a XML document that declares the DTD to use for validation
	 * and thus that DTD validation is being used.
	 */
	private static final String DOCTYPE = "DOCTYPE";

	/**
	 * The token that indicates the start of an XML comment.
	 */
	private static final String START_COMMENT = "<!--";

	/**
	 * The token that indicates the end of an XML comment.
	 */
	private static final String END_COMMENT = "-->";


	/**
	 * Indicates whether or not the current parse position is inside an XML comment.<br/>
	 * 是否为注释
	 */
	private boolean inComment;


	/**
	 * Detect the validation mode for the XML document in the supplied {@link InputStream}.
	 * Note that the supplied {@link InputStream} is closed by this method before returning.
	 * @param inputStream the InputStream to parse
	 * @throws IOException in case of I/O failure
	 * @see #VALIDATION_DTD
	 * @see #VALIDATION_XSD
	 */
	public int detectValidationMode(InputStream inputStream) throws IOException {
		// Peek into the file to look for DOCTYPE.
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		try {
			// 是否为 DTD 校验模式。默认为，非 DTD 模式，即 XSD 模式
			boolean isDtdValidated = false;
			String content;
			// 循环，逐行读取 XML 文件的内容
			while ((content = reader.readLine()) != null) {
				content = consumeCommentTokens(content);
				// 如果是注释或内容为空，则跳过
				if (this.inComment || !StringUtils.hasText(content)) {
					continue;
				}
				// 包含 DOCTYPE 为 DTD 模式
				if (hasDoctype(content)) {
					isDtdValidated = true;
					break;
				}
				// hasOpeningTag 方法会校验，如果这一行有 < ，并且 < 后面跟着的是字母，则退出循环
				if (hasOpeningTag(content)) {
					// End of meaningful data...
					break;
				}
			}
			return (isDtdValidated ? VALIDATION_DTD : VALIDATION_XSD);
		}
		catch (CharConversionException ex) {
			// Choked on some character encoding...
			// Leave the decision up to the caller.
			// 如果抛异常，则返回 自动模式
			return VALIDATION_AUTO;
		}
		finally {
			reader.close();
		}
	}


	/**
	 * Does the content contain the DTD DOCTYPE declaration?
	 */
	private boolean hasDoctype(String content) {
		return content.contains(DOCTYPE);
	}

	/**
	 * Does the supplied content contain an XML opening tag. If the parse state is currently
	 * in an XML comment then this method always returns false. It is expected that all comment
	 * tokens will have consumed for the supplied content before passing the remainder to this method.
	 */
	private boolean hasOpeningTag(String content) {
		if (this.inComment) {
			return false;
		}
		int openTagIndex = content.indexOf('<');
		return (openTagIndex > -1 && (content.length() > openTagIndex + 1) &&
				Character.isLetter(content.charAt(openTagIndex + 1)));
	}

	/**
	 * Consumes all the leading comment data in the given String and returns the remaining content, which
	 * may be empty since the supplied content might be all comment data. For our purposes it is only important
	 * to strip leading comment content on a line since the first piece of non comment content will be either
	 * the DOCTYPE declaration or the root element of the document.
	 * 我们的目的是：只关注当前行 剥离注释内容后的 第一块非注释内容 是否是一个 DOCTYPE 的声明 或者 是 document的root元素
	 */
	@Nullable
	private String consumeCommentTokens(String line) {
		// 非注释，直接返回当前行的内容
		if (!line.contains(START_COMMENT) && !line.contains(END_COMMENT)) {
			return line;
		}
		String currLine = line;
		// 循环处理
		while ((currLine = consume(currLine)) != null) {
			// 不是注释，并且不以 <!-- 开头，则返回当前行
			if (!this.inComment && !currLine.trim().startsWith(START_COMMENT)) {
				return currLine;
			}
		}
		return null;
	}

	/**
	 * Consume the next comment token, update the "inComment" flag
	 * and return the remaining content.
	 */
	@Nullable
	private String consume(String line) {
		// 第一次执行时：inComment最初值为false，执行 startComment() 方法，该方法 执行完成后 inComment 赋值为true
		// 第二次执行时：inComment已赋值为true，会执行 endComment（）方法，该方法 执行完成后 inComment 赋值为false
		// 相当于inComment交替执行 startComment() 方法 和 endComment（）方法
		int index = (this.inComment ? endComment(line) : startComment(line));
		// 如果不包含注释，返回Null，如果包含注释，返回注释后面的内容
		return (index == -1 ? null : line.substring(index));
	}

	/**
	 * Try to consume the {@link #START_COMMENT} token.
	 * @see #commentToken(String, String, boolean)
	 */
	private int startComment(String line) {
		return commentToken(line, START_COMMENT, true);
	}

	private int endComment(String line) {
		return commentToken(line, END_COMMENT, false);
	}

	/**
	 * Try to consume the supplied token against the supplied content and update the
	 * in comment parse state to the supplied value. Returns the index into the content
	 * which is after the token or -1 if the token is not found.
	 */
	private int commentToken(String line, String token, boolean inCommentIfPresent) {
		int index = line.indexOf(token);
		if (index > - 1) {
			// 给 inComment 赋值，如果包含 START_COMMENT，则赋值为true，如果为 END_COMMENT，则赋值为false
			this.inComment = inCommentIfPresent;
		}
		// 如果不包含注释，则返回-1，否则返回 注释后面内容的索引值
		return (index == -1 ? index : index + token.length());
	}

}
