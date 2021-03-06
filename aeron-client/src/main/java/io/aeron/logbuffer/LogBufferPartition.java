/*
 * Copyright 2014 - 2016 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.aeron.logbuffer;

import org.agrona.concurrent.UnsafeBuffer;

import static io.aeron.logbuffer.LogBufferDescriptor.TERM_TAIL_COUNTER_OFFSET;

/**
 * Log buffer implementation containing common functionality for dealing with log partition terms.
 */
public class LogBufferPartition
{
    private final UnsafeBuffer termBuffer;
    private final UnsafeBuffer metaDataBuffer;

    public LogBufferPartition(final UnsafeBuffer termBuffer, final UnsafeBuffer metaDataBuffer)
    {
        this.termBuffer = termBuffer;
        this.metaDataBuffer = metaDataBuffer;
    }

    /**
     * The log of messages for a term.
     *
     * @return the log of messages for a term.
     */
    public UnsafeBuffer termBuffer()
    {
        return termBuffer;
    }

    /**
     * The meta data describing the term.
     *
     * @return the meta data describing the term.
     */
    public UnsafeBuffer metaDataBuffer()
    {
        return metaDataBuffer;
    }

    /**
     * Get the current tail value in a volatile memory ordering fashion. If raw tail is greater than
     * {@link #termBuffer()}.{@link org.agrona.DirectBuffer#capacity()} then capacity will be returned.
     *
     * @return the current tail value.
     */
    public int tailOffsetVolatile()
    {
        final long tail = metaDataBuffer.getLongVolatile(TERM_TAIL_COUNTER_OFFSET) & 0xFFFF_FFFFL;

        return (int)Math.min(tail, (long)termBuffer.capacity());
    }

    /**
     * Get the raw value for the tail containing both termId and offset.
     *
     * @return the raw value for the tail containing both termId and offset.
     */
    public long rawTailVolatile()
    {
        return metaDataBuffer.getLongVolatile(TERM_TAIL_COUNTER_OFFSET);
    }

    /**
     * Set the value of the term id into the tail counter.
     *
     * @param termId for the tail counter
     */
    public void termId(final int termId)
    {
        metaDataBuffer.putLong(TERM_TAIL_COUNTER_OFFSET, ((long)termId) << 32);
    }

    /**
     * Get the value of the term id into the tail counter.
     *
     * @return the current term id.
     */
    public int termId()
    {
        final long rawTail = metaDataBuffer.getLong(TERM_TAIL_COUNTER_OFFSET);

        return (int)(rawTail >>> 32);
    }
}
