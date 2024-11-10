package io.logscope;

import io.logscope.message.Message;
import io.logscope.message.MessageLevel;
import net.minecraft.text.Text;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;

public class LogScope implements LogScopeSink {
    static final LogScope INSTANCE = new LogScope();

    private final ArrayDeque<Message> messages = new ArrayDeque<>();

    @Override
    public void logMessage(@NotNull MessageLevel level, @NotNull Text text) {
        Validate.notNull(level);
        Validate.notNull(text);

        this.messages.addLast(new Message(level, text.copy()));
    }

    public Deque<Message> getMessageDrain() {
        return this.messages;
    }

    public static LogScopeSink instance() {
        return INSTANCE;
    }
}